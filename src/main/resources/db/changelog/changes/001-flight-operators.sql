--liquibase formatted sql

--changeset flight-platform:001-create-clients
CREATE TABLE flight_operators (
    -- represents client/customer/tenant who can create various flight plans

    id UUID PRIMARY KEY,

    -- unique identifier for a client. Used in external references & APIs
    client_key VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_operator_status CHECK (status IN ('ACTIVE', 'DISABLED'))
);