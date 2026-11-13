package com.codrshi.pg_traffic_control.debezium;

import com.codrshi.pg_traffic_control.kafka.IFlightCaptureService;
import io.debezium.engine.ChangeEvent;
import io.debezium.engine.RecordChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DbzFlightListener {

    @Autowired
    private IFlightCaptureService flightCaptureService;

    // SourceRecord = kafka connect data structure representing record produced by the source connector.
    // SourceRecord contains Source partition, source offset, topic name, key, value (change event payload), headers

    // RecordChangeEvent = debezium wrapper around SourceRecord
    public void handleFlight(RecordChangeEvent<SourceRecord> flight) {
        SourceRecord sourceRecord = flight.record();

        if (sourceRecord != null && sourceRecord.value() != null) {
            log.info("Flight Captured: {}", sourceRecord);

            // business logic
            flightCaptureService.capture(sourceRecord.toString());
        }
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
