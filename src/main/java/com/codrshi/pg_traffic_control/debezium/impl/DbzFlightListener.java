package com.codrshi.pg_traffic_control.debezium.impl;

import com.codrshi.pg_traffic_control.debezium.IDbzFlightListener;
import com.codrshi.pg_traffic_control.kafka.IFlightCaptureService;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class DbzFlightListener implements IDbzFlightListener {

    @Autowired
    private IFlightCaptureService flightCaptureService;

    // SourceRecord = kafka connect data structure representing record produced by the source connector.
    // SourceRecord contains Source partition, source offset, topic name, key, value (change event payload), headers

    // RecordChangeEvent = debezium wrapper around SourceRecord
    @Override
    public void handleFlight(List<RecordChangeEvent<SourceRecord>> flights, DebeziumEngine.RecordCommitter<RecordChangeEvent<SourceRecord>> committer, UUID flightPlanId) throws InterruptedException {
        for (RecordChangeEvent<SourceRecord> flight : flights) {
            SourceRecord sourceRecord = flight.record();

            if (sourceRecord != null && sourceRecord.value() != null) {
                log.info("Flight Captured: {}", sourceRecord);

                // Blocks until Kafka ACKs; markProcessed skipped in case of error
                flightCaptureService.capture(sourceRecord, flightPlanId);

                // debezium commits offset
                committer.markProcessed(flight);
            }
        }

        // flush to offset table in postgres after batch completion.
        committer.markBatchFinished();
    }


    // SourceRecord:
    /**
    {
        "topic": "cdc-events.public.inventory_items",
        "key": {
                  "id": 2
        },
        "value": {
           "before": null,
           "after": {
                    "id": 2,
                    "sku": "ITEM-002",
                    "item_name": "Traffic Sensor Node",
                    "quantity": 25,
                    "unit_price": 149.99,
                    "updated_at": "2026-11-13T07:16:26.264679Z"
           },
           "source": {
                   "version": "3.7.0.Final",
                    "connector": "postgresql",
                    "name": "cdc-events",
                    "ts_ms": 1794554186264,
                    "snapshot": null,
                    "db": "pg_traffic_control",
                    "schema": "public",
                    "table": "inventory_items",
                    "txId": 770,
                    "lsn": 29720360
           },
           "op": "c",
           "ts_ms": 1794554186706
        },
        "headers": {
        "__debezium.context.connectorLogicalName": "cdc-events",
                "__debezium.context.taskId": "0",
                "__debezium.context.connectorName": "postgresql",
                "__debezium.context.runId": "01a1d3c3-8cf9-78f9-9400-ad3014fb898d"
    }
    }
    **/
}
