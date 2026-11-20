package com.codrshi.pg_traffic_control.util.preflight_inspection_stages;

import com.codrshi.pg_traffic_control.common.enums.DatabaseType;
import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionContext;
import com.codrshi.pg_traffic_control.entity.DatabaseConnection;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.util.PreflightInspectionStage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class DbTypeCheckPfiStage extends PreflightInspectionStage {
    public DbTypeCheckPfiStage(PreflightInspectionStage nextPfiStage) {
        super(nextPfiStage);
    }

    @Override
    public void inspect(PreflightInspectionContext preflightInspectionContext, List<PfiFailureCode> failures) {
        log.info("INSPECTION STAGE: Validating database types of connections.");

        boolean hasNonPostgresDb = preflightInspectionContext.databaseConnections().stream()
                .anyMatch(conn -> !DatabaseType.POSTGRESQL.equals(conn.getDatabaseType()));

        if(hasNonPostgresDb) {
            addFailureCode(failures, PfiFailureCode.UNSUPPORTED_DATABASE_TYPE);
        }

        if(nextPfiStage != null) {
            nextPfiStage.inspect(preflightInspectionContext, failures);
        }
    }
}
