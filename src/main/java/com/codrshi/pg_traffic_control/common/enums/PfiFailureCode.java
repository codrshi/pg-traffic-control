package com.codrshi.pg_traffic_control.common.enums;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum PfiFailureCode {
    ORIGIN_CONN_COUNT_INVALID("A flight plan must have exactly one origin connection."),
    DESTINATION_CONN_COUNT_INVALID("A flight plan must have exactly one destination connection."),
    NO_CAPTURE_RUNAWAYS_FOUND("A flight plan must have at-least one table from origin database to capture."),
    UNSUPPORTED_DATABASE_TYPE("Only PostgreSQL connections are supported correctly."),
    DB_CONN_HANDSHAKE_FAILED("DB connection details should be correct to make it reachable over the network."),
    ORIGIN_REPLICATION_PRIVILEGE_MISSING("The origin DB should grant postgreSQL replication privileges."),
    ORIGIN_DB_WAL_LEVEL_INVALID("The origin DB must have wal_level set to logical."),
    ORIGIN_DB_REPLICATION_SLOTS_INVALID("The origin DB must allow at least one replication slot."),
    ORIGIN_DB_MAX_WAL_SENDERS_INVALID("The origin DB must allow at least one WAL sender."),
    DB_TABLE_NOT_FOUND("The capture table(s) should exist in DB."),
    DB_SCHEMA_USAGE_MISSING("The origin or destination DB should grant USAGE privilege on schema(s)."),
    ORIGIN_TABLE_SELECT_MISSING("The origin DB should grant SELECT privilege on source table(s)."),
    ORIGIN_DB_PUBLICATION_MODE_INVALID("The origin DB should configure publication_mode as MANUAL or AUTO_FILTERED."),
    ORIGIN_DB_PUBLICATION_NAME_MISSING("The origin DB should have configured publication_name for publication_mode = MANUAL."),
    ORIGIN_TABLE_NOT_IN_PUBLICATION("The origin table(s) should be included in pg_publication_tables."),
    ORIGIN_DB_CREATE_PUBLICATION_MISSING_PRIVILEGE("The origin DB should grant CREATE privilege for publication_mode = AUTO_FILTERED."),
    ORIGIN_TABLE_OWNERSHIP_MISSING("The origin DB should grant table ownership or owner role for publication_mode = AUTO_FILTERED."),
    ORIGIN_TABLE_INSERT_MISSING("The destination DB should grant INSERT privilege on audit table(s).");

    private final String message;

    PfiFailureCode(String message) {
        this.message = message;
    }

    // Invoked by Jackson to build JSON response for PreflightInspectionResponse
    public String getCode() {
        return this.name();
    }

    // Invoked by Jackson to build JSON response for PreflightInspectionResponse
    public String getMessage() {
        return this.message;
    }
}
