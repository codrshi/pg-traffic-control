package com.codrshi.pg_traffic_control.debezium.config;

import com.codrshi.pg_traffic_control.common.Constant;
import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import com.codrshi.pg_traffic_control.common.properties.DatabaseProperties;
import com.codrshi.pg_traffic_control.entity.CaptureRunway;
import com.codrshi.pg_traffic_control.entity.DatabaseConnection;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.exception.ResourceNotFoundException;

import java.util.Map;
import java.util.stream.Collectors;

public class DbzConfigFactory {

    public static io.debezium.config.Configuration build(DatabaseProperties databaseProperties, FlightPlan flightPlan) {

        Map<String, Object> originDbConn = extractOriginDbConn(flightPlan);
        String originRunaways = extractOriginRunaways(flightPlan);

        // debezium substitutes %%s with table name
        String selectQuery = String.format(
                "SELECT id, offset_key, offset_val FROM %%s WHERE flight_plan_id = '%s' ORDER BY record_insert_ts ASC, record_insert_seq ASC",
                flightPlan.getId()
        );
        String insertQuery = String.format(
                "INSERT INTO %%s (id, flight_plan_id, offset_key, offset_val, record_insert_ts, record_insert_seq) VALUES (?, '%s', ?, ?, ?, ?)",
                flightPlan.getId()
        );
        String deleteQuery = String.format(
                "DELETE FROM %%s WHERE id = ? AND flight_plan_id = '%s'",
                flightPlan.getId()
        );

        return io.debezium.config.Configuration.create()
                // custom identifier for the debezium connector to identify it in logs
                .with("name", "pgtc-dbz_" + flightPlan.getId())
                // load postgres-specific connector implementation
                .with("connector.class", "io.debezium.connector.postgresql.PostgresConnector")

                // offset = LSN of WAL logs upto which debezium has processed successfully.
                // FileOffsetBackingStore = storing offset progress in local file with name offsetStorageFile
                // JdbcOffsetBackingStore = storing offset progress in database table
                // and flush interval of 60s
                .with("offset.storage", "io.debezium.storage.jdbc.offset.JdbcOffsetBackingStore")
                // TODO: fine-tune & move to application.yaml
                .with("offset.flush.interval.ms", "60000")
                .with("offset.flush.timeout.ms", "80000")
                .with("offset.storage.jdbc.connection.wait.retry.delay.ms", "3000")
                .with("offset.storage.jdbc.connection.retry.max.attempts", "10")
                .with("offset.storage.jdbc.connection.url", databaseProperties.url())
                // TODO: create seperate user to INSERT, SELECT, DELETE from offset table.
                .with("offset.storage.jdbc.connection.user", databaseProperties.username())
                .with("offset.storage.jdbc.connection.password", databaseProperties.password())
                .with("offset.storage.jdbc.table.name", "debezium_runtime.debezium_offset_storage")
                .with("offset.storage.jdbc.table.select", selectQuery)
                .with("offset.storage.jdbc.table.insert", insertQuery)
                .with("offset.storage.jdbc.table.delete", deleteQuery)

                // Connect to DB and use logical replication to consume WAL logs. The user must have replication privileges
                .with("database.hostname", originDbConn.get(Constant.DB_CONN_JSON_COLUMN_HOST))
                .with("database.port", originDbConn.get(Constant.DB_CONN_JSON_COLUMN_PORT))
                .with("database.user", originDbConn.get(Constant.DB_CONN_JSON_COLUMN_USERNAME))
                .with("database.password", originDbConn.get(Constant.DB_CONN_JSON_COLUMN_PASSWORD))
                .with("database.dbname", originDbConn.get(Constant.DB_CONN_JSON_COLUMN_DB_NAME))

                // Debezium specific properties
                //topic.prefix = namespace for your connector's change event topics
                .with("topic.prefix", "flight-event_" + flightPlan.getId()) // Prefix for engine identification
                .with("plugin.name", "pgoutput")     // Uses native PG logical replication

                // specific tables to capture
                // resultant topic name: cdc-events.public.inventory_items
                .with("table.include.list", originRunaways)
                .with("tombstones.on.delete", "false")

                .build();
    }

    private static Map<String, Object> extractOriginDbConn(FlightPlan flightPlan) {
        return flightPlan.getDatabaseConnections().stream()
                .filter(conn -> DbConnectionRole.ORIGIN.equals(conn.getConnectionRole()))
                .map(DatabaseConnection::getConnectionDetails)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(String.format("Origin details not found for flight plan ID = %s", flightPlan.getId())));
    }

    private static String extractOriginRunaways(FlightPlan flightPlan) {
        return flightPlan.getCaptureRunaways().stream()
                .filter(CaptureRunway::isEnabled)
                .filter(runaway -> runaway.getSourceSchema()!=null && runaway.getSourceTable()!=null)
                .map(runaway -> runaway.getSourceSchema().trim() + "." + runaway.getSourceTable().trim())
                .collect(Collectors.joining(","));
    }
}
