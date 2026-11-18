package com.codrshi.pg_traffic_control.dto.response;


import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import lombok.Builder;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record CreateFlightPlanResponse(
        UUID planId,
        String planName,
        FlightPlanStatus status,
        Integer version,
        Instant createdAt,
        Instant updatedAt
) {}