package com.codrshi.pg_traffic_control.service.impl;

import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionContext;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.exception.ResourceNotFoundException;
import com.codrshi.pg_traffic_control.repository.FlightPlanRepository;
import com.codrshi.pg_traffic_control.service.IFlightPlanStateService;
import org.hibernate.Hibernate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FlightPlanStateService implements IFlightPlanStateService {

    @Autowired
    private FlightPlanRepository flightPlanRepository;

    @Override
    @Transactional
    public PreflightInspectionContext startInspection(UUID flightPlanId) {
        FlightPlan flightPlan = flightPlanRepository.findById(flightPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("No flight plan exist for given ID."));

        if (FlightPlanStatus.DECOMMISSIONED.equals(flightPlan.getStatus())) {
            throw new IllegalStateException("Flight plan is decommissioned.");
        }
        if (FlightPlanStatus.INSPECTING.equals(flightPlan.getStatus())) {
            throw new IllegalStateException("Preflight inspection is ongoing.");
        }

        // prevent LazyInitializationException by explicitly fetching related entities within transaction scope
        Hibernate.initialize(flightPlan.getCaptureRunaways());
        Hibernate.initialize(flightPlan.getDatabaseConnections());

        flightPlan.setStatus(FlightPlanStatus.INSPECTING);
        flightPlanRepository.save(flightPlan);

        return new PreflightInspectionContext(flightPlanId, flightPlan.getCaptureRunaways(), flightPlan.getDatabaseConnections());
    }

    @Override
    @Transactional
    public FlightPlan completeInspection(UUID flightPlanId, FlightPlanStatus finalStatus) {
        FlightPlan flightPlan = flightPlanRepository.findById(flightPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("No flight plan exist for given ID."));

//        uncomment into overloaded methods if needed
//        Hibernate.initialize(flightPlan.getCaptureRunaways());
//        Hibernate.initialize(flightPlan.getDatabaseConnections());

        flightPlan.setStatus(finalStatus);
        return flightPlanRepository.saveAndFlush(flightPlan);
    }
}
