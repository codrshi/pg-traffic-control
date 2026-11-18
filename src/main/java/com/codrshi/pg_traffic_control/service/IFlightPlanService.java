package com.codrshi.pg_traffic_control.service;

import com.codrshi.pg_traffic_control.dto.request.CreateFlightPlanRequest;
import com.codrshi.pg_traffic_control.dto.response.CreateFlightPlanResponse;

public interface IFlightPlanService {
    CreateFlightPlanResponse createPlan(String clientKey, CreateFlightPlanRequest request);
}
