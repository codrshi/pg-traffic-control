package com.codrshi.pg_traffic_control.util.preflight_inspection_stages;

import com.codrshi.pg_traffic_control.common.Constant;
import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionContext;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionFailure;
import com.codrshi.pg_traffic_control.entity.CaptureRunway;
import com.codrshi.pg_traffic_control.entity.DatabaseConnection;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.util.AuditSchemaValidator;
import com.codrshi.pg_traffic_control.util.DbUtil;
import com.codrshi.pg_traffic_control.util.PreflightInspectionStage;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static com.codrshi.pg_traffic_control.common.Constant.QUERY_TIMEOUT_SECONDS;

@Slf4j
public class DestinationConnCheckPfiStage extends PreflightInspectionStage {

    public DestinationConnCheckPfiStage(PreflightInspectionStage nextPfiStage) {
        super(nextPfiStage);
    }

    @Override
    public void inspect(PreflightInspectionContext preflightInspectionContext, List<PreflightInspectionFailure> failures) {
        log.info("INSPECTION STAGE: Validating destination network connection & privileges.");

        DatabaseConnection destinationConn = preflightInspectionContext.databaseConnections().stream()
                .filter(conn -> DbConnectionRole.DESTINATION.equals(conn.getConnectionRole()))
                .findFirst().orElse(null);

        if (destinationConn == null) {
            log.warn("missing destination connection details for flightPlanId = {}", preflightInspectionContext.planId());
            if(nextPfiStage != null) {
                nextPfiStage.inspect(preflightInspectionContext, failures);
            }
            return;
        }

        try (Connection connection = DbUtil.openConnection(destinationConn.getConnectionDetails())) {
            validateDestinationTables(connection, preflightInspectionContext.captureRunways(), failures);
            validateAuditSchema(connection, preflightInspectionContext.captureRunways(), failures);
        } catch (Exception e) {
            log.warn("Encountered unknown error in {}: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            addFailureCode(failures, PfiFailureCode.DB_CONN_HANDSHAKE_FAILED.of("destination"));
        } finally {
            if(nextPfiStage != null) {
                nextPfiStage.inspect(preflightInspectionContext, failures);
            }
        }
    }

    private void validateDestinationTables(Connection connection, List<CaptureRunway> runaways, List<PreflightInspectionFailure> failures) throws SQLException {
        for (CaptureRunway runaway : runaways) {
            if (!runaway.isEnabled()) {
                continue;
            }

            String qualifiedName = runaway.getAuditSchema() + "." + runaway.getAuditTable();

            try (PreparedStatement statement = connection.prepareStatement(Constant.DB_QUERY_CHECK_INSERT_PRIVILEGE)) {
                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                statement.setString(1, qualifiedName);
                statement.setString(2, runaway.getAuditSchema());
                statement.setString(3, qualifiedName);
                statement.setString(4, qualifiedName);

                try (ResultSet rs = statement.executeQuery()) {
                    rs.next();

                    if (!rs.getBoolean("table_exists")) {
                        failures.add(PfiFailureCode.DB_TABLE_NOT_FOUND.of(qualifiedName, "destination"));
                        continue;
                    }
                    if (!rs.getBoolean("can_use_schema")) {
                        failures.add(PfiFailureCode.DB_SCHEMA_USAGE_MISSING.of("destination", qualifiedName));
                    }
                    if (!rs.getBoolean("can_insert")) {
                        failures.add(PfiFailureCode.ORIGIN_TABLE_SELECT_MISSING.of(qualifiedName));
                    }
                }
            }
        }
    }

    private void validateAuditSchema(Connection connection, List<CaptureRunway> runaways, List<PreflightInspectionFailure> failures) throws SQLException {
        for (CaptureRunway runaway : runaways) {
            if (!runaway.isEnabled()) {
                continue;
            }

            String qualifiedName = runaway.getAuditSchema() + "." + runaway.getAuditTable();
            AuditSchemaValidator auditSchemaValidator = AuditSchemaValidator.getInstance();

            try (PreparedStatement statement = connection.prepareStatement(Constant.DB_QUERY_CHECK_TABLE_COLUMNS)) {
                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                statement.setString(1, runaway.getAuditSchema());
                statement.setString(2, runaway.getAuditTable());

                try (ResultSet rs = statement.executeQuery()) {
                    while (rs.next()) {
                        auditSchemaValidator.add(
                                rs.getString("column_name"),
                                rs.getString("is_nullable"),
                                rs.getString("data_type")
                        );
                    }
                }
            }

            List<String> invalidColumns = auditSchemaValidator.validate();
            if (!invalidColumns.isEmpty()) {
                addFailureCode(failures, PfiFailureCode.INVALID_AUDIT_SCHEMA.of(qualifiedName, invalidColumns));
            }
        }
    }
}
