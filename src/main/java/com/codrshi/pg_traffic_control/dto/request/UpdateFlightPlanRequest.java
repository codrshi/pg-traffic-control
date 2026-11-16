package com.codrshi.pg_traffic_control.dto.request;

public record UpdateFlightPlanRequest(
        String name,
        Integer version,
        String originConnectionUrl,
        String destinationConnectionUrl,
        String defaultAuditTable,
        String runaways
) {}
