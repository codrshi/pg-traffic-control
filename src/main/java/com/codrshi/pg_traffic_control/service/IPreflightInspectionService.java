package com.codrshi.pg_traffic_control.service;

import com.codrshi.pg_traffic_control.dto.response.PreflightInspectionResponse;

import java.util.UUID;

public interface IPreflightInspectionService {
    PreflightInspectionResponse inspectPlan(UUID flightPlanId);
}
