package com.codrshi.pg_traffic_control.dto.response;


import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record FlightPlanResponse(
        UUID id,
        UUID flightOperatorId,
        String name,
        FlightPlanStatus status,
        Integer version,
        List<DatabaseConnectionResponse> databaseConnections,
        List<CaptureRunawayResponse> captureRunaways,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {

    public record DatabaseConnectionResponse(
            UUID id,
            String connectionRole,
            String databaseType,
            String host,
            Integer port,
            String databaseName,
            String username,
            String sslMode,
            String defaultAuditSchema,
            String defaultAuditTable
    ) {}

    public record CaptureRunawayResponse(
            UUID id,
            String sourceSchema,
            String sourceTable,
            String auditSchema,
            String auditTable,
            boolean enabled
    ) {}
}