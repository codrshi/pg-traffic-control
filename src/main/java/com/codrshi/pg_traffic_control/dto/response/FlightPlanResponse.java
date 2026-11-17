package com.codrshi.pg_traffic_control.dto.response;


import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import lombok.Builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Builder
public record FlightPlanResponse(
        UUID planId,
        String planName,
        FlightPlanStatus status,
        Integer version,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}