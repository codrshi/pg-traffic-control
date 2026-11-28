package com.codrshi.pg_traffic_control.dto.event;

import com.codrshi.pg_traffic_control.util.AuditSchemaValidator;

import java.util.Map;
import java.util.UUID;

public record FlightCaptureEvent(
        UUID eventId,
        UUID flightPlanId,
        String opType,
        Long msts,
        String tableName,
        Map<String, Object> beforeData,
        Map<String, Object> afterData
) {}
