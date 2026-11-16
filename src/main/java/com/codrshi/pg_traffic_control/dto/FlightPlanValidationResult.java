package com.codrshi.pg_traffic_control.dto;

import java.util.List;

public record FlightPlanValidationResult(
        boolean valid,
        List<String> errors
) {}
