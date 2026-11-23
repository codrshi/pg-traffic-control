package com.codrshi.pg_traffic_control.common.enums;

import com.codrshi.pg_traffic_control.dto.PreflightInspectionFailure;
import org.slf4j.helpers.MessageFormatter;

public enum PfiFailureCode {
    DB_CONN_COUNT_INVALID("A flight plan must have exactly one {} connection."),
    NO_CAPTURE_RUNAWAYS_FOUND("A flight plan must have at-least one table from origin database to capture."),
    UNSUPPORTED_DATABASE_TYPE("Only PostgreSQL connections are supported currently."),
    DB_CONN_HANDSHAKE_FAILED("{} DB connection details should be correct to make it reachable over the network."),
    ORIGIN_REPLICATION_PRIVILEGE_MISSING("The origin DB should grant postgreSQL replication privileges."),
    ORIGIN_DB_WAL_LEVEL_INVALID("The origin DB must have wal_level set to logical."),
    ORIGIN_DB_REPLICATION_SLOTS_INVALID("The origin DB must allow at least one replication slot."),
    ORIGIN_DB_MAX_WAL_SENDERS_INVALID("The origin DB must allow at least one WAL sender."),
    DB_TABLE_NOT_FOUND("The capture table {} is not present in {} DB."),
    DB_SCHEMA_USAGE_MISSING("The {} DB should grant USAGE privilege on schema {}."),
    ORIGIN_TABLE_SELECT_MISSING("The origin DB should grant SELECT privilege on table {}"),
    ORIGIN_TABLE_NOT_IN_PUBLICATION("The origin table {} should be included in pg_publication_tables."),
    ORIGIN_TABLE_INSERT_MISSING("The destination DB should grant INSERT privilege on audit table {}."),
    INVALID_AUDIT_SCHEMA("The audit table {} has following field(s) invalid: {}");

    private final String messageTemplate;

    PfiFailureCode(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public PreflightInspectionFailure of() {
        return new PreflightInspectionFailure(this, this.messageTemplate);
    }

    public PreflightInspectionFailure of(Object... args) {
        String formattedMessage = MessageFormatter.arrayFormat(this.messageTemplate, args).getMessage();
        return new PreflightInspectionFailure(this, formattedMessage);
    }
}
