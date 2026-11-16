package com.codrshi.pg_traffic_control.service;

import com.codrshi.pg_traffic_control.dto.request.CreateFlightPlanRequest;
import com.codrshi.pg_traffic_control.dto.response.FlightPlanResponse;

public interface IFlightPlanService {
    FlightPlanResponse createPlan(String clientKey, CreateFlightPlanRequest request);
}
