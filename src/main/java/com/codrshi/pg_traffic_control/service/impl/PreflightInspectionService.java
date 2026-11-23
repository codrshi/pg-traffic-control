package com.codrshi.pg_traffic_control.service.impl;

import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionContext;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionFailure;
import com.codrshi.pg_traffic_control.dto.response.PreflightInspectionResponse;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.service.IFlightPlanStateService;
import com.codrshi.pg_traffic_control.service.IPreflightInspectionService;
import com.codrshi.pg_traffic_control.util.PreflightInspectionStage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PreflightInspectionService implements IPreflightInspectionService {


    @Autowired
    private PreflightInspectionStage preflightInspectionChain;

    @Autowired
    private IFlightPlanStateService flightPlanStateService;

    @Override
    public PreflightInspectionResponse inspectPlan(UUID flightPlanId) {

        PreflightInspectionContext preflightInspectionContext = flightPlanStateService.startInspection(flightPlanId);

        try {
            List<PreflightInspectionFailure> failures = runPreflightInspection(preflightInspectionContext);
            FlightPlanStatus inspectionStatus = failures.isEmpty() ? FlightPlanStatus.CLEARED : FlightPlanStatus.GROUNDED;

            FlightPlan flightPlan = flightPlanStateService.completeInspection(flightPlanId, inspectionStatus);
            return buildResponse(flightPlan.getName(), flightPlan.getStatus(), failures);
        }
        catch (Exception ex) {
            flightPlanStateService.completeInspection(flightPlanId, FlightPlanStatus.DRAFT);
            throw ex;
        }
    }

    private List<PreflightInspectionFailure> runPreflightInspection(PreflightInspectionContext preflightInspectionContext) {
        List<PreflightInspectionFailure> failures = new ArrayList<>();
        preflightInspectionChain.inspect(preflightInspectionContext, failures);
        return failures;
    }

    private PreflightInspectionResponse buildResponse(String name, FlightPlanStatus status, List<PreflightInspectionFailure> failures) {
        return PreflightInspectionResponse.builder()
                .planName(name)
                .status(status)
                .inspectionFailures(failures)
                .build();
    }
}
