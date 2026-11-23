package com.codrshi.pg_traffic_control.util.preflight_inspection_stages;

import com.codrshi.pg_traffic_control.common.Constant;
import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionContext;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionFailure;
import com.codrshi.pg_traffic_control.entity.CaptureRunway;
import com.codrshi.pg_traffic_control.entity.DatabaseConnection;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.util.DbUtil;
import com.codrshi.pg_traffic_control.util.PreflightInspectionStage;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.codrshi.pg_traffic_control.common.Constant.QUERY_TIMEOUT_SECONDS;

@Slf4j
public class OriginConnCheckPfiStage extends PreflightInspectionStage {

    public OriginConnCheckPfiStage(PreflightInspectionStage nextPfiStage) {
        super(nextPfiStage);
    }

    @Override
    public void inspect(PreflightInspectionContext preflightInspectionContext, List<PreflightInspectionFailure> failures) {
        log.info("INSPECTION STAGE: Validating origin network connection & privileges.");

        DatabaseConnection originConn = preflightInspectionContext.databaseConnections().stream()
                .filter(conn -> DbConnectionRole.ORIGIN.equals(conn.getConnectionRole()))
                .findFirst().orElse(null);

        if (originConn == null) {
            log.warn("missing origin connection details for flightPlanId = {}", preflightInspectionContext.planId());
            if(nextPfiStage != null) {
                nextPfiStage.inspect(preflightInspectionContext, failures);
            }
            return;
        }

        try (Connection connection = DbUtil.openConnection(originConn.getConnectionDetails())) {
            validateLogicalReplication(connection, failures);
            validateReplicationCapacity(connection, failures);
            validateSourceTables(connection, preflightInspectionContext.captureRunways(), failures);
            validatePublicationConfiguration(connection, preflightInspectionContext.captureRunways(), failures);
        } catch (Exception e) {
            log.warn("Encountered unknown error in {}: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            addFailureCode(failures, PfiFailureCode.DB_CONN_HANDSHAKE_FAILED.of("origin"));
        } finally {
            if(nextPfiStage != null) {
                nextPfiStage.inspect(preflightInspectionContext, failures);
            }
        }
    }

    private void validateLogicalReplication(Connection connection, List<PreflightInspectionFailure> failures) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_CHECK_REPLICATION_PRIVILEGE)) {
                if (!rs.next() || !rs.getBoolean("can_replicate")) {
                    failures.add(PfiFailureCode.ORIGIN_REPLICATION_PRIVILEGE_MISSING.of());
                }
            }
        }
    }

    private void validateReplicationCapacity(Connection connection, List<PreflightInspectionFailure> failures) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_SHOW_WAL_LEVEL)) {
                if (!rs.next() || !"logical".equalsIgnoreCase(rs.getString(1))) {
                    failures.add(PfiFailureCode.ORIGIN_DB_WAL_LEVEL_INVALID.of());
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_SHOW_MAX_REPLICATION_SLOTS)) {
                if (!rs.next() || rs.getInt(1) < 1) {
                    failures.add(PfiFailureCode.ORIGIN_DB_REPLICATION_SLOTS_INVALID.of());
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_SHOW_MAX_WAL_SENDERS)) {
                if (!rs.next() || rs.getInt(1) < 1) {
                    failures.add(PfiFailureCode.ORIGIN_DB_MAX_WAL_SENDERS_INVALID.of());
                }
            }
        }

    }

    private void validateSourceTables(Connection connection, List<CaptureRunway> runaways, List<PreflightInspectionFailure> failures) throws SQLException {
        for (CaptureRunway runaway : runaways) {
            if (!runaway.isEnabled()) {
                continue;
            }

            String qualifiedName = runaway.getSourceSchema() + "." + runaway.getSourceTable();

            try (PreparedStatement statement = connection.prepareStatement(Constant.DB_QUERY_CHECK_SELECT_PRIVILEGE)) {

                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                statement.setString(1, qualifiedName);
                statement.setString(2, runaway.getSourceSchema());
                statement.setString(3, qualifiedName);
                statement.setString(4, qualifiedName);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();

                    if (!rs.getBoolean("table_exists")) {
                        failures.add(PfiFailureCode.DB_TABLE_NOT_FOUND.of(qualifiedName, "origin"));
                        continue;
                    }
                    if (!rs.getBoolean("can_use_schema")) {
                        failures.add(PfiFailureCode.DB_SCHEMA_USAGE_MISSING.of("origin", qualifiedName));
                    }
                    if (!rs.getBoolean("can_select")) {
                        failures.add(PfiFailureCode.ORIGIN_TABLE_SELECT_MISSING.of(qualifiedName));
                    }
                }
            }
        }
    }

    private void validatePublicationConfiguration(Connection connection, List<CaptureRunway> runaways, List<PreflightInspectionFailure> failures) throws SQLException {
        for (CaptureRunway runaway : runaways) {
            String qualifiedName = runaway.getSourceSchema() + "." + runaway.getSourceTable();

            try (PreparedStatement statement = connection.prepareStatement(Constant.DB_QUERY_CHECK_PUBLICATION_TABLES)) {

                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                statement.setString(1, runaway.getPublicationName());
                statement.setString(2, runaway.getSourceSchema());
                statement.setString(3, runaway.getSourceTable());

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();

                    if (!rs.getBoolean("table_is_published")) {
                        failures.add(PfiFailureCode.ORIGIN_TABLE_NOT_IN_PUBLICATION.of(qualifiedName));
                    }
                }
            }
        }
    }
}
