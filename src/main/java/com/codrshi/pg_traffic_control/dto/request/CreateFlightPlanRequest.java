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
        String publicationName,
        @NotEmpty(message = "At least one runaway must be provided")
        List<Runaway> runaways
) {
        public record Runaway(String departure, String arrival) {}
}

// {
//         "name": "Inventory Audit Flight",
//         "originConnectionUrl": "jdbc:postgresql://localhost:5432/origin_db?sslmode=disable&user=origin_user&password=password",
//         "destinationConnectionUrl": "jdbc:postgresql://localhost:5432/destination_db?sslmode=disable&user=destination_user&password=password",
//         "defaultAuditTable": "audit.change_audit",
//         "publicationName": "dbz_publication"
//         "runaways": [
//          {
//           "departure": "public.inventory_items",
//           "arrival": "public.inventory_items_audit"
//          },
//          {
//           "departure": "public.orders",
//           "arrival": "public.orders_audit"
//          }
//         ]
//  }

//CREATE USER origin_user WITH PASSWORD 'password';
//GRANT USAGE ON SCHEMA public TO origin_user;
//GRANT SELECT ON ALL TABLES IN SCHEMA public TO origin_user;
//GRANT CONNECT ON DATABASE origin_db TO origin_user;
//CREATE PUBLICATION dbz_publication FOR TABLE public.inventory_items, public.orders;

//CREATE TABLE public.cdc_audit_events (
//        -- PK, idempotency
//        event_id TEXT PRIMARY KEY,
//        -- flight plan ID to which it belongs
//        flight_plan_id TEXT NOT NULL,
//        -- type of operation on source table: c,r,u,d
//        operation VARCHAR(1) NOT NULL,
//        -- PK of source table
//        record_key JSONB NOT NULL,
//        -- schema.table name of source table
//        captured_table VARCHAR(255) NOT NULL,
//        -- time at which event happened in source DB
//        captured_at TIMESTAMP WITH TIME ZONE NOT NULL,
//        -- time at which event was recorded in recieved DB
//        received_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
//        before_data JSONB,
//        after_data JSONB,
//        metadata JSONB,
//
//        CONSTRAINT chk_operation_type CHECK (operation IN ('c','r','u','d'))
//);



//CREATE USER destination_user WITH PASSWORD 'password';
//GRANT CONNECT ON DATABASE destination_db TO destination_user;
//GRANT USAGE ON SCHEMA public TO destination_user;
//GRANT INSERT ON ALL TABLES IN SCHEMA public TO destination_user;