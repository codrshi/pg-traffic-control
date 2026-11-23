package com.codrshi.pg_traffic_control.util;

import com.codrshi.pg_traffic_control.common.Constant;
import com.codrshi.pg_traffic_control.dto.SchemaTable;
import com.codrshi.pg_traffic_control.exception.UnprocessableContentException;
import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class DbUtil {

    private static final int CONNECT_TIMEOUT_SECONDS = 5;
    private static final int SOCKET_TIMEOUT_SECONDS = 8;

    public static SchemaTable resolveSchema(String schemaTable) {

        String[] value = schemaTable.trim().split("\\.");

        if(value.length > 2 || value.length == 0) {
            throw new UnprocessableContentException("Invalid table reference provided. Use schema.table notation.");
        }

        return value.length == 1? new SchemaTable(Constant.DEFAULT_SCHEMA, value[0]): new SchemaTable(value[0], value[1]);
    }

    public static Map<String, Object> resolveConnectionDetails(String url) {

        String jdbcUrl = url.trim();

        if (jdbcUrl.startsWith("postgres://")) {
            jdbcUrl = "jdbc:postgresql://" + jdbcUrl.substring("postgres://".length());
        } else if (jdbcUrl.startsWith("postgresql://")) {
            jdbcUrl = "jdbc:" + jdbcUrl;
        }

        Properties props = Optional.ofNullable(Driver.parseURL(jdbcUrl, null))
                .orElseThrow(() -> new UnprocessableContentException("Invalid postgresql URL connection provided."));

        return Map.of(
                Constant.DB_CONN_JSON_COLUMN_HOST, props.getProperty("PGHOST"),
                Constant.DB_CONN_JSON_COLUMN_PORT, props.getProperty("PGPORT", "5432"),
                Constant.DB_CONN_JSON_COLUMN_DB_NAME, props.getProperty("PGDBNAME"),
                Constant.DB_CONN_JSON_COLUMN_USERNAME, props.getProperty("user"),
                Constant.DB_CONN_JSON_COLUMN_PASSWORD, props.getProperty("password"),
                Constant.DB_CONN_JSON_COLUMN_SSL_MODE, props.getProperty("sslmode")
        );
    }

    public static Connection openConnection(Map<String, Object> config) throws SQLException {
        String host = config.get(Constant.DB_CONN_JSON_COLUMN_HOST).toString().trim();
        int port = Integer.parseInt(String.valueOf(config.get(Constant.DB_CONN_JSON_COLUMN_PORT)));
        String database = config.get(Constant.DB_CONN_JSON_COLUMN_DB_NAME).toString().trim();
        String username = config.get(Constant.DB_CONN_JSON_COLUMN_USERNAME).toString().trim();
        String password = config.get(Constant.DB_CONN_JSON_COLUMN_PASSWORD).toString().trim();
        String sslMode = config.get(Constant.DB_CONN_JSON_COLUMN_SSL_MODE).toString().trim();

        String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + database;

        Properties properties = new Properties();
        properties.setProperty("user", username);
        properties.setProperty("password", password);
        properties.setProperty("sslmode", sslMode);
        properties.setProperty(
                "connectTimeout",
                String.valueOf(CONNECT_TIMEOUT_SECONDS)
        );
        properties.setProperty(
                "socketTimeout",
                String.valueOf(SOCKET_TIMEOUT_SECONDS)
        );
        properties.setProperty(
                "ApplicationName",
                "pg-traffic-control-validation"
        );

        return DriverManager.getConnection(jdbcUrl, properties);
    }
}
