
--liquibase formatted sql

--changeset flight-platform:004-create-capture-targets
CREATE TABLE capture_targets (
    -- represents the tables to capture as per flight plan.
    id UUID PRIMARY KEY,
    flight_plan_id UUID NOT NULL,
    source_schema VARCHAR(150) NOT NULL,
    source_table VARCHAR(150) NOT NULL,

    -- optional destination DB schema override for audit
    audit_schema VARCHAR(150),
    -- optional destination DB table override for audit
    audit_table VARCHAR(150),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_capture_targets_flight_plan FOREIGN KEY (flight_plan_id) REFERENCES flight_plans(id),

     CONSTRAINT uk_flight_plan_source_table UNIQUE (flight_plan_id, source_schema, source_table)
);

CREATE INDEX idx_capture_targets_flight_plan ON capture_targets(flight_plan_id);
