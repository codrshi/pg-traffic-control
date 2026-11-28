package com.codrshi.pg_traffic_control.debezium;

import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import org.apache.kafka.connect.source.SourceRecord;

import java.util.List;
import java.util.UUID;

public interface IDbzFlightListener {
    void handleFlight(List<RecordChangeEvent<SourceRecord>> flights, DebeziumEngine.RecordCommitter<RecordChangeEvent<SourceRecord>> committer, UUID flightPlanId) throws InterruptedException;
}
