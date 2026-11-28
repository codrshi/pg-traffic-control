--liquibase formatted sql

--changeset flight-platform:005-create-debezium-offset-storage

CREATE SCHEMA IF NOT EXISTS debezium_runtime;

CREATE TABLE debezium_runtime.debezium_offset_storage (
      id VARCHAR(36) NOT NULL,
      flight_plan_id UUID NOT NULL,
      offset_key VARCHAR(1255),
      offset_val VARCHAR(1255),
      record_insert_ts TIMESTAMP NOT NULL,
      record_insert_seq INTEGER NOT NULL,

      CONSTRAINT pk_debezium_offset_storage PRIMARY KEY (id)
);

CREATE INDEX idx_debezium_offset_storage ON debezium_runtime.debezium_offset_storage (record_insert_ts, record_insert_seq);
CREATE INDEX idx_debezium_offset_plan_id ON debezium_runtime.debezium_offset_storage (flight_plan_id);