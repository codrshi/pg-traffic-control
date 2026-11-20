package com.codrshi.pg_traffic_control.util.preflight_inspection_stages;

import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionContext;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.util.PreflightInspectionStage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class ConnCountPfiStage extends PreflightInspectionStage {

    public ConnCountPfiStage(PreflightInspectionStage nextPfiStage) {
        super(nextPfiStage);
    }

    @Override
    public void inspect(PreflightInspectionContext preflightInspectionContext, List<PfiFailureCode> failures) {
        log.info("INSPECTION STAGE: Validating origin & destination connection count.");

        long originConnCount = preflightInspectionContext.databaseConnections().stream().filter(conn -> DbConnectionRole.ORIGIN.equals(conn.getConnectionRole())).count();
        if(originConnCount != 1L) {
            addFailureCode(failures, PfiFailureCode.ORIGIN_CONN_COUNT_INVALID);
        }

        long destinationConnCount = preflightInspectionContext.databaseConnections().stream().filter(conn -> DbConnectionRole.DESTINATION.equals(conn.getConnectionRole())).count();
        if(destinationConnCount != 1L) {
            addFailureCode(failures, PfiFailureCode.DESTINATION_CONN_COUNT_INVALID);
        }

        if(nextPfiStage != null) {
            nextPfiStage.inspect(preflightInspectionContext, failures);
        }
    }
}
