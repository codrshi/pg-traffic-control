package com.codrshi.pg_traffic_control.util.preflight_inspection_stages;

import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.entity.CaptureRunway;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.util.PreflightInspectionStage;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class CaptureRunawayCountPfiStage extends PreflightInspectionStage {
    public CaptureRunawayCountPfiStage(PreflightInspectionStage nextPfiStage) {
        super(nextPfiStage);
    }

    @Override
    public void inspect(FlightPlan flightPlan, List<PfiFailureCode> failures) {
        log.info("INSPECTION STAGE: Validating capture runaways presence.");

        boolean hasCaptureRunaway = flightPlan.getCaptureRunaways().stream().anyMatch(CaptureRunway::isEnabled);

        if(!hasCaptureRunaway) {
            addFailureCode(failures, PfiFailureCode.NO_CAPTURE_RUNAWAYS_FOUND);
        }

        if(nextPfiStage != null) {
            nextPfiStage.inspect(flightPlan, failures);
        }
    }
}
