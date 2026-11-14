--liquibase formatted sql

--changeset flight-platform:002-create-flight-plans
CREATE TABLE flight_plans (
    -- represents a CDC pipeline config for flights (events) from origin DB-to-destination DB
    -- each client/operator can setup multiple flight plans

    id UUID PRIMARY KEY,
    flight_operator_id UUID NOT NULL,
    name VARCHAR(200) NOT NULL,

    -- status of flight plan. It doesnt reflects runtime status of flights
    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',
    version INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_flight_plans_client FOREIGN KEY (flight_operator_id) REFERENCES flight_operators(id),

    -- draft: plan is just created
    -- validating: the platform is validating plan (origin db, destination db, permission, tables)
    -- ready: plan is ready to use
    -- active: plan is enabled for execution
    -- paused:  operator/platform has paused it
    -- failed: validation failed
    -- decommissioned: permanently deleted
    CONSTRAINT chk_plan_status CHECK (status IN ('DRAFT', 'VALIDATING', 'READY', 'ACTIVE', 'PAUSED', 'FAILED', 'DECOMMISSIONED'))
);

CREATE INDEX idx_flight_plans_client ON flight_plans(flight_operator_id);
