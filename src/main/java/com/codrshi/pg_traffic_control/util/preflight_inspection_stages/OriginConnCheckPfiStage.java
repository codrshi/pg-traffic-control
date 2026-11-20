package com.codrshi.pg_traffic_control.util.preflight_inspection_stages;

import com.codrshi.pg_traffic_control.common.Constant;
import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
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
    public void inspect(FlightPlan flightPlan, List<PfiFailureCode> failures) {
        log.info("INSPECTION STAGE: Validating origin network connection & privileges.");

        DatabaseConnection originConn = flightPlan.getDatabaseConnections().stream()
                .filter(conn -> DbConnectionRole.ORIGIN.equals(conn.getConnectionRole()))
                .findFirst().orElse(null);

        if (originConn == null) {
            log.warn("missing origin connection details for flightPlanId = {}", flightPlan.getId());
            if(nextPfiStage != null) {
                nextPfiStage.inspect(flightPlan, failures);
            }
            return;
        }

        try (Connection connection = DbUtil.openConnection(originConn.getConnectionDetails())) {
            validateLogicalReplication(connection, failures);
            validateReplicationCapacity(connection, failures);
            validateSourceTables(connection, flightPlan.getCaptureRunaways(), failures);
            validatePublicationConfiguration(connection, originConn.getConnectionDetails(), flightPlan.getCaptureRunaways(), failures);
        } catch (Exception e) {
            log.warn("Encountered unknown error in {}: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            addFailureCode(failures, PfiFailureCode.DB_CONN_HANDSHAKE_FAILED);
        } finally {
            if(nextPfiStage != null) {
                nextPfiStage.inspect(flightPlan, failures);
            }
        }
    }

    private void validateLogicalReplication(Connection connection, List<PfiFailureCode> failures) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_CHECK_REPLICATION_PRIVILEGE)) {
                if (!rs.next() || !rs.getBoolean("can_replicate")) {
                    failures.add(PfiFailureCode.ORIGIN_REPLICATION_PRIVILEGE_MISSING);
                }
            }
        }
    }

    private void validateReplicationCapacity(Connection connection, List<PfiFailureCode> failures) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_SHOW_WAL_LEVEL)) {
                if (!rs.next() || !"logical".equalsIgnoreCase(rs.getString(1))) {
                    failures.add(PfiFailureCode.ORIGIN_DB_WAL_LEVEL_INVALID);
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_SHOW_MAX_REPLICATION_SLOTS)) {
                if (!rs.next() || rs.getInt(1) < 1) {
                    failures.add(PfiFailureCode.ORIGIN_DB_REPLICATION_SLOTS_INVALID);
                }
            }
        }

        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_SHOW_MAX_WAL_SENDERS)) {
                if (!rs.next() || rs.getInt(1) < 1) {
                    failures.add(PfiFailureCode.ORIGIN_DB_MAX_WAL_SENDERS_INVALID);
                }
            }
        }

    }

    private void validateSourceTables(Connection connection, List<CaptureRunway> runaways, List<PfiFailureCode> failures) throws SQLException {
        for (CaptureRunway runaway : runaways) {
            if (!runaway.isEnabled()) {
                continue;
            }

            String qualifiedName = runaway.getSourceSchema() + ":" + runaway.getSourceTable();

            try (PreparedStatement statement = connection.prepareStatement(Constant.DB_QUERY_CHECK_SELECT_PRIVILEGE)) {

                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                statement.setString(1, qualifiedName);
                statement.setString(2, runaway.getSourceSchema());
                statement.setString(3, qualifiedName);
                statement.setString(4, qualifiedName);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();

                    if (!rs.getBoolean("table_exists")) {
                        failures.add(PfiFailureCode.DB_TABLE_NOT_FOUND);
                        continue;
                    }
                    if (!rs.getBoolean("can_use_schema")) {
                        failures.add(PfiFailureCode.DB_SCHEMA_USAGE_MISSING);
                    }
                    if (!rs.getBoolean("can_select")) {
                        failures.add(PfiFailureCode.ORIGIN_TABLE_SELECT_MISSING);
                    }
                }
            }
        }
    }

    private void validatePublicationConfiguration(Connection connection, Map<String, Object> config, List<CaptureRunway> runaways, List<PfiFailureCode> failures) throws SQLException {
        if (config.get("publication_mode") == null) {
            failures.add(PfiFailureCode.ORIGIN_DB_PUBLICATION_MODE_INVALID);
            return;
        }

        String publicationMode = config.get("publication_mode").toString().trim();

        switch (publicationMode.toUpperCase(Locale.ROOT)) {
            case "MANUAL" -> validateManualPublication(connection, config, runaways, failures);
            case "AUTO_FILTERED" -> validateAutomaticPublication(connection, runaways, failures);
            default -> failures.add(PfiFailureCode.ORIGIN_DB_PUBLICATION_MODE_INVALID);
        }
    }

    private void validateManualPublication(Connection connection, Map<String, Object> config, List<CaptureRunway> runaways, List<PfiFailureCode> failures) throws SQLException {
        if (config.get("publication_name") == null) {
            failures.add(PfiFailureCode.ORIGIN_DB_PUBLICATION_NAME_MISSING);
            return;
        }

        String publicationName = config.get("publication_name").toString().trim();

        for (CaptureRunway runaway : runaways) {
            try (PreparedStatement statement = connection.prepareStatement(Constant.DB_QUERY_CHECK_PUBLICATION_TABLES)) {

                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                statement.setString(1, publicationName);
                statement.setString(2, runaway.getSourceSchema());
                statement.setString(3, runaway.getSourceTable());

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();

                    if (!rs.getBoolean("table_is_published")) {
                        failures.add(PfiFailureCode.ORIGIN_TABLE_NOT_IN_PUBLICATION);
                    }
                }
            }
        }
    }

    private void validateAutomaticPublication(Connection connection, List<CaptureRunway> runaways, List<PfiFailureCode> failures) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);

            try (ResultSet rs = statement.executeQuery(Constant.DB_QUERY_CHECK_CREATE_PUBLICATION_PRIVILEGE)) {
                rs.next();

                if (!rs.getBoolean("can_create_publication")) {
                    failures.add(PfiFailureCode.ORIGIN_DB_CREATE_PUBLICATION_MISSING_PRIVILEGE);
                }
            }
        }

        for (CaptureRunway runaway : runaways) {
            try (PreparedStatement statement = connection.prepareStatement(Constant.DB_QUERY_CHECK_TABLE_OWNERSHIP)) {

                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                statement.setString(1, runaway.getSourceSchema());
                statement.setString(2, runaway.getSourceTable());

                try (ResultSet rs = statement.executeQuery()) {
                    if (!rs.next() || !rs.getBoolean("can_manage_table")) {
                        failures.add(PfiFailureCode.ORIGIN_TABLE_OWNERSHIP_MISSING);
                    }
                }
            }
        }
    }
}
