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
    -- inspecting: the platform is running pre-flight inspection to validate plan (origin db, destination db, permission, tables)
    -- cleared: plan is ready to use (preflight inspection successful)
    -- grounded: plan failed in validation (preflight inspection failed)
    -- airborne: plan is currently executing in CDC engine & monitored by ATC
    -- paused:  operator/platform has paused the plan execution
    -- aborted: plan has failed to execute or failed mid-execution
    -- decommissioned: permanently deleted
    CONSTRAINT chk_plan_status CHECK (status IN ('DRAFT', 'INSPECTING', 'CLEARED', 'GROUNDED', 'AIRBORNE', 'PAUSED', 'ABORTED', 'DECOMMISSIONED'))
);

CREATE INDEX idx_flight_plans_client ON flight_plans(flight_operator_id);
