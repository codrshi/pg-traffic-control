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
                .with("name", "postgres-debezium-engine")
                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")

                // Offset management (where to save progress)
                .with("offset.storage", "org.apache.kafka.connect.storage.FileOffsetBackingStore")
                .with("offset.storage.file.filename", offsetStorageFile)
                .with("offset.flush.interval.ms", "60000")

                // Database connection
                .with("database.hostname", dbHost)
                .with("database.port", dbPort)
                .with("database.user", dbUser)
                .with("database.password", dbPassword)
                .with("database.dbname", dbDatabase)

                // Debezium specific requirements
                .with("topic.prefix", "cdc-events") // Prefix for engine identification
                .with("plugin.name", "pgoutput")     // Uses native PG logical replication

                // Schema history configuration
                .with("schema.history.internal", "io.debezium.storage.file.history.FileSchemaHistory")
                .with("schema.history.internal.file.filename", schemaHistoryFile)

                // Optional filters: Uncomment if you only want specific tables captured
                .with("table.include.list", "public.inventory_items")

                .build();
    }
}
