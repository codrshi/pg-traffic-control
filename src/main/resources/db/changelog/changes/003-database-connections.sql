--liquibase formatted sql

--changeset flight-platform:003-create-database-connections
CREATE TABLE database_connections (
    -- represents the origin and source DB connection details for a flight plan.
    id UUID PRIMARY KEY,
    flight_plan_id UUID NOT NULL,

    -- identifies whether connection represents origin DB or source DB
    connection_role VARCHAR(20) NOT NULL,
    database_type VARCHAR(30) NOT NULL,
    connection_details JSONB NOT NULL,

    -- default destination DB schema where audit records can be stored
    default_audit_schema VARCHAR(150),
    -- default destination DB table
    default_audit_table VARCHAR(150),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_connections_flight_plan FOREIGN KEY (flight_plan_id) REFERENCES flight_plans(id),

    CONSTRAINT uk_flight_plan_connection_role UNIQUE (flight_plan_id, connection_role),

    CONSTRAINT chk_connection_role CHECK (connection_role IN ('ORIGIN', 'DESTINATION')),

    -- only postgres supported as of now
    CONSTRAINT chk_database_type CHECK (database_type IN ('POSTGRES')),

    CONSTRAINT chk_connection_details_keys CHECK (
        jsonb_typeof(connection_details) = 'object' AND
        jsonb_exists_all(connection_details, ARRAY['host', 'port', 'database_name', 'username', 'password', 'ssl_mode'])
        ),

    CONSTRAINT chk_connection_port CHECK ((connection_details->>'port')::INTEGER BETWEEN 1 AND 65535),

    CONSTRAINT chk_connection_ssl_mode CHECK ((connection_details->>'ssl_mode') IN ('disable', 'allow', 'prefer', 'require', 'verify-ca', 'verify-full'))
);

CREATE INDEX idx_connections_flight_plan ON database_connections(flight_plan_id);
