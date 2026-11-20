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
import java.util.List;

import static com.codrshi.pg_traffic_control.common.Constant.QUERY_TIMEOUT_SECONDS;

@Slf4j
public class DestinationConnCheckPfiStage extends PreflightInspectionStage {

    public DestinationConnCheckPfiStage(PreflightInspectionStage nextPfiStage) {
        super(nextPfiStage);
    }

    @Override
    public void inspect(FlightPlan flightPlan, List<PfiFailureCode> failures) {
        log.info("INSPECTION STAGE: Validating destination network connection & privileges.");

        DatabaseConnection destinationConn = flightPlan.getDatabaseConnections().stream()
                .filter(conn -> DbConnectionRole.DESTINATION.equals(conn.getConnectionRole()))
                .findFirst().orElse(null);

        if (destinationConn == null) {
            log.warn("missing destination connection details for flightPlanId = {}", flightPlan.getId());
            if(nextPfiStage != null) {
                nextPfiStage.inspect(flightPlan, failures);
            }
            return;
        }

        try (Connection connection = DbUtil.openConnection(destinationConn.getConnectionDetails())) {
            validateDestinationTables(connection, flightPlan.getCaptureRunaways(), failures);
        } catch (Exception e) {
            log.warn("Encountered unknown error in {}: {}", this.getClass().getSimpleName(), e.getMessage(), e);
            addFailureCode(failures, PfiFailureCode.DB_CONN_HANDSHAKE_FAILED);
        } finally {
            if(nextPfiStage != null) {
                nextPfiStage.inspect(flightPlan, failures);
            }
        }
    }

    private void validateDestinationTables(Connection connection, List<CaptureRunway> runaways, List<PfiFailureCode> failures) throws SQLException {
        for (CaptureRunway runaway : runaways) {
            if (!runaway.isEnabled()) {
                continue;
            }

            String qualifiedName = runaway.getAuditSchema() + ":" + runaway.getAuditTable();

            try (PreparedStatement statement = connection.prepareStatement(Constant.DB_QUERY_CHECK_INSERT_PRIVILEGE)) {
                statement.setQueryTimeout(QUERY_TIMEOUT_SECONDS);
                statement.setString(1, qualifiedName);
                statement.setString(2, runaway.getAuditSchema());
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
                    if (!rs.getBoolean("can_insert")) {
                        failures.add(PfiFailureCode.ORIGIN_TABLE_SELECT_MISSING);
                    }
                }
            }
        }
    }
}
