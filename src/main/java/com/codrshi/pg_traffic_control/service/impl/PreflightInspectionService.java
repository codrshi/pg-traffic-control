package com.codrshi.pg_traffic_control.service.impl;

import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionFailure;
import com.codrshi.pg_traffic_control.dto.response.PreflightInspectionResponse;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.exception.ResourceNotFoundException;
import com.codrshi.pg_traffic_control.repository.FlightPlanRepository;
import com.codrshi.pg_traffic_control.service.IPreflightInspectionService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PreflightInspectionService implements IPreflightInspectionService {

    @Autowired
    private FlightPlanRepository flightPlanRepository;

    @Override
    @Transactional
    public PreflightInspectionResponse inspectPlan(UUID flightPlanId) {

        FlightPlan flightPlan = retrieveAndValidatePlan(flightPlanId);

        flightPlan = updatePlanStatus(flightPlan, FlightPlanStatus.INSPECTING);

        List<PreflightInspectionFailure> failures = runPreflightInspection(flightPlan);
        FlightPlanStatus inspectionStatus = failures.isEmpty()? FlightPlanStatus.CLEARED :FlightPlanStatus.GROUNDED;

        flightPlan = updatePlanStatus(flightPlan, inspectionStatus);
        return buildResponse(flightPlan.getName(), flightPlan.getStatus(), failures);
    }

    private List<PreflightInspectionFailure> runPreflightInspection(FlightPlan flightPlan) {
        return new ArrayList<>();
    }

    private FlightPlan retrieveAndValidatePlan(UUID flightPlanId) {
        FlightPlan flightPlan = flightPlanRepository.findById(flightPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("No flight plan exist for given ID."));

        if(FlightPlanStatus.DECOMMISSIONED.equals(flightPlan.getStatus())) {
            throw new IllegalStateException("Flight plan is decommissioned.");
        }

        if(FlightPlanStatus.INSPECTING.equals(flightPlan.getStatus())) {
            throw new IllegalStateException("Preflight inspection is ongoing.");
        }

        return flightPlan;
    }

    private FlightPlan updatePlanStatus(FlightPlan flightPlan, FlightPlanStatus status) {
        flightPlan.setStatus(status);
        return flightPlanRepository.saveAndFlush(flightPlan);
    }

    private PreflightInspectionResponse buildResponse(String name, FlightPlanStatus status, List<PreflightInspectionFailure> failures) {
        return PreflightInspectionResponse.builder()
                .planName(name)
                .status(status)
                .inspectionFailures(failures)
                .build();
    }
}
