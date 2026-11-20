package com.codrshi.pg_traffic_control.service;

import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionContext;
import com.codrshi.pg_traffic_control.entity.FlightPlan;

import java.util.UUID;

public interface IFlightPlanStateService {
    PreflightInspectionContext startInspection(UUID flightPlanId);
    FlightPlan completeInspection(UUID flightPlanId, FlightPlanStatus finalStatus);
}
