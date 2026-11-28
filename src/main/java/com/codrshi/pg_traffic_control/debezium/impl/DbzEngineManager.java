package com.codrshi.pg_traffic_control.debezium.impl;

import com.codrshi.pg_traffic_control.common.properties.DatabaseProperties;
import com.codrshi.pg_traffic_control.debezium.IDbzEngineManager;
import com.codrshi.pg_traffic_control.debezium.IDbzFlightListener;
import com.codrshi.pg_traffic_control.debezium.config.DbzConfigFactory;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.exception.ResourceNotFoundException;
import com.codrshi.pg_traffic_control.repository.FlightPlanRepository;
import io.debezium.embedded.Connect;
import io.debezium.engine.DebeziumEngine;
import io.debezium.engine.RecordChangeEvent;
import io.debezium.engine.format.ChangeEventFormat;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.connect.source.SourceRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DbzEngineManager implements IDbzEngineManager {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Autowired
    private DatabaseProperties databaseProperties;

    @Autowired
    private FlightPlanRepository flightPlanRepository;

    @Autowired
    private IDbzFlightListener dbzFlightListener;

    @Override
    public DebeziumEngine<RecordChangeEvent<SourceRecord>> startEngine(UUID flightPlanId) {
        FlightPlan flightPlan = flightPlanRepository.findById(flightPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight Plan not found."));

        io.debezium.config.Configuration dbzConfig = DbzConfigFactory.build(databaseProperties, flightPlan);

        // Build the embedded engine
        // ChangeEventFormat provides various event format representations like:
        // - Connect.class (engine will deliver events Kafka Connect SourceRecord objects wrapped in RecordChangeEvent)
        // - Json.class ( engine will deliver events in JSON representation. Useful when directly consuming as an object.)
        //
        // AsyncEmbeddedEngine is used which uses an internal thread pool to async process change event using the callback.
        // number of worker threads (record.processing.threads) is Dynamically allocated based on workload and available CPU cores
        // by default, record.processing.order = ORDERED, which means records are delivered while preserving their original order
        // TODO: override record.processing.threads and compare performance
        return DebeziumEngine.create(ChangeEventFormat.of(Connect.class))
                .using(dbzConfig.asProperties())
                // notifying registers callback that debezium invokes whenever it delivers a change event.
                // TODO: replace single-record lambda with batch processing and record acknowlegment
                .notifying((flights, committer) -> dbzFlightListener.handleFlight(flights, committer, flightPlanId))
                .using((success, message, error) -> {
                    if (!success) {
                        log.error("Debezium engine failed: {}", message, error);
                    } else {
                        log.info("Debezium engine stopped: {}", message);
                    }
                })
                .build();
    }

    @Override
    private void stopEngine(DebeziumEngine<RecordChangeEvent<SourceRecord>> engine) throws IOException, InterruptedException {
        if (engine != null) {
            engine.close();
        }
        this.executor.shutdown();
        if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
            executor.shutdownNow();
        }
    }
}
