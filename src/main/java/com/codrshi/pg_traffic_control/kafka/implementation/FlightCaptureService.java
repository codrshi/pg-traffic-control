package com.codrshi.pg_traffic_control.kafka.implementation;

import org.apache.kafka.connect.data.Field;
import org.apache.kafka.connect.data.Struct;
import com.codrshi.pg_traffic_control.common.properties.KafkaProperties;
import com.codrshi.pg_traffic_control.config.KafkaConfig;
import com.codrshi.pg_traffic_control.dto.event.FlightCaptureEvent;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.kafka.IFlightCaptureService;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class FlightCaptureService implements IFlightCaptureService {

    @Autowired
    private KafkaTemplate<String, FlightCaptureEvent> kafkaTemplate;

    @Autowired
    private KafkaProperties kafkaProperties;

    @Override
    public void capture(SourceRecord record, UUID flightPlanId) {
        String topicName = kafkaProperties.topics().get("pgtc-flight-v0").name();
        String partitionKey = extractPrimaryKey(record);
        FlightCaptureEvent payload = buildEventPayload(record, flightPlanId);

        try {
            // publish synchronously so Debezium only commits offset on broker ACK
            // TODO: handle null partition key
            kafkaTemplate.send(topicName, partitionKey, payload).get();
        } catch (InterruptedException e) {
            // TODO: handle with DLQ
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while capturing flight event.", e);
        } catch (ExecutionException e) {
            throw new RuntimeException("Failed to capture flight event", e.getCause());
        }
    }

    private String extractPrimaryKey(SourceRecord record) {
        if (record.key() instanceof Struct keyStruct) {
            Field pkField = keyStruct.schema().fields().get(0);
            Object pkValue = keyStruct.get(pkField);
            return pkValue != null ? pkValue.toString() : null;
        }
        return record.key() != null ? record.key().toString() : null;
    }

    private FlightCaptureEvent buildEventPayload(SourceRecord record, UUID flightPlanId) {
        Struct valueStruct = (Struct) record.value();

        // handling tombstone events having null valueStruct
        if (valueStruct == null) {
            return new FlightCaptureEvent(
                    UUID.randomUUID(),
                    flightPlanId,
                    "tombstone",
                    System.currentTimeMillis(),
                    null,
                    null,
                    null);
        }

        String op = valueStruct.getString("op");
        Long tsMs = valueStruct.getInt64("ts_ms");
        String tableName = extractTableName(valueStruct);
        Map<String, Object> before = structToMap(valueStruct.getStruct("before"));
        Map<String, Object> after = structToMap(valueStruct.getStruct("after"));

        return new FlightCaptureEvent(
                UUID.randomUUID(),
                flightPlanId,
                op,
                tsMs,
                tableName,
                before,
                after
        );
    }

    private String extractTableName(Struct valueStruct) {
        Struct source = valueStruct.getStruct("source");
        return source.getString("schema") + "." + source.getString("table");
    }

    private Map<String, Object> structToMap(Struct struct) {
        if (struct == null) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        for (Field field : struct.schema().fields()) {
            map.put(field.name(), struct.get(field));
        }
        return map;
    }
}
