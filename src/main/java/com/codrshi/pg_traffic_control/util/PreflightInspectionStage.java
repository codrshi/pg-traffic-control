package com.codrshi.pg_traffic_control.util;

import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.entity.FlightPlan;

import java.util.List;

public abstract class PreflightInspectionStage {
    protected PreflightInspectionStage nextPfiStage;

    public PreflightInspectionStage(PreflightInspectionStage nextPfiStage) {
        this.nextPfiStage = nextPfiStage;
    }

    public void addFailureCode(List<PfiFailureCode> failures, PfiFailureCode failureCode) {
        failures.add(failureCode);
    }

    public abstract void inspect(FlightPlan flightPlan, List<PfiFailureCode> failures);
}
