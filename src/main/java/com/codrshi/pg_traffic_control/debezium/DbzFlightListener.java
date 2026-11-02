package com.codrshi.pg_traffic_control.debezium;

import io.debezium.engine.ChangeEvent;
import io.debezium.engine.RecordChangeEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class DbzFlightListener {

    public void handleFlight(RecordChangeEvent<SourceRecord> flight) {
        SourceRecord sourceRecord = flight.record();

        if (sourceRecord != null && sourceRecord.value() != null) {
            log.info("Flight Captured: {}", sourceRecord);

            // business logic
        }
    }
}
