package com.codrshi.pg_traffic_control.debezium;

import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.UUID;

public interface IDbzEngineManager {
    DebeziumEngine<RecordChangeEvent<SourceRecord>> startEngine(UUID flightPlanId);
}
