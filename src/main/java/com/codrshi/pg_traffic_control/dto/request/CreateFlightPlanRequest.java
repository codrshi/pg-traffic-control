package com.codrshi.pg_traffic_control.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateFlightPlanRequest(
        String name,
        @NotBlank(message = "Origin connection URL is required and cannot be blank")
        String originConnectionUrl,
        @NotBlank(message = "Destination connection URL is required and cannot be blank")
        String destinationConnectionUrl,
        String defaultAuditTable,
        @NotEmpty(message = "At least one runaway must be provided")
        List<Runaway> runaways
) {
        public record Runaway(String departure, String arrival) {}
}

/**
 * {
 *   "name": "Inventory Audit Flight",
 *   "originConnectionUrl": "jdbc:postgresql://user:pass@host:5432/db?sslmode=require",
 *   "destinationConnectionUrl": "jdbc:postgresql://user:pass@host:5432/db?sslmode=require",
 *   "defaultAuditTable": "audit.change_audit",
 *   "runaways": [
 *     {
 *       "departure": "public.inventory_items",
 *       "arrival": "public.inventory_items_audit"
 *     },
 *     {
 *        "departure": "public.inventory_items",
 *        "arrival": "public.inventory_items_audit"
 *     }
 *  ]
 * }
 */
