package com.codrshi.pg_traffic_control.util;

import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionContext;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionFailure;

import java.util.List;

public abstract class PreflightInspectionStage {
    protected PreflightInspectionStage nextPfiStage;

    public PreflightInspectionStage(PreflightInspectionStage nextPfiStage) {
        this.nextPfiStage = nextPfiStage;
    }

    public void addFailureCode(List<PreflightInspectionFailure> failures, PreflightInspectionFailure failure) {
        failures.add(failure);
    }

    public abstract void inspect(PreflightInspectionContext preflightInspectionContext, List<PreflightInspectionFailure> failures);
}
