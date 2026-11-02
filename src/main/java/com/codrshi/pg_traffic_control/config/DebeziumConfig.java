package com.codrshi.pg_traffic_control.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DebeziumConfig {
    @Value("${debezium.datasource.host}")
    private String dbHost;

    @Value("${debezium.datasource.port}")
    private String dbPort;

    @Value("${debezium.datasource.user}")
    private String dbUser;

    @Value("${debezium.datasource.password}")
    private String dbPassword;

    @Value("${debezium.datasource.database}")
    private String dbDatabase;

    @Value("${debezium.offset.storage.file.filename}")
    private String offsetStorageFile;

    @Value("${debezium.schema.history.internal.file.filename}")
    private String schemaHistoryFile;

    @Bean
    public io.debezium.config.Configuration postgresConnector() {
        return io.debezium.config.Configuration.create()
                // custom identifier for the debezium connector to identify it in logs
                .with("name", "postgres-debezium-engine")
                // load postgres-specific connector implementation
                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")

                // offset = LSN of WAL logs upto which debezium has processed successfully.
                // FileOffsetBackingStore = storing offset progress in local file with name offsetStorageFile
                // and flush interval of 60s
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", offsetStorageFile)
                .with("offset.flush.interval.ms", "60000")

                // Connect to DB and use logical replication to consume WAL logs. The user must have replication privileges
                .with("database.hostname", dbHost)
                .with("database.port", dbPort)
                .with("database.user", dbUser)
                .with("database.password", dbPassword)
                .with("database.dbname", dbDatabase)

                // Debezium specific properties
                //topic.prefix = namespace for your connector's change event topics
                .with("topic.prefix", "cdc-events") // Prefix for engine identification
                .with("plugin.name", "pgoutput")     // Uses native PG logical replication

                // Schema history configuration
                // implementation to use for storing internal schema history of captured tables.
                // Debezium needs to understand the schema that was applicable when a particular change occurred.
                // FileSchemaHistory = file-based schema history.
                .with("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory")
                .with("schema.history.internal.file.filename", schemaHistoryFile)

                // specific tables to capture
                // resultant topic name: cdc-events.public.inventory_items
                .with("table.include.list", "public.inventory_items")

                .build();
    }
}
