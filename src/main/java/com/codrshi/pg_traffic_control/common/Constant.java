package com.codrshi.pg_traffic_control.common;

public class Constant {
    public static final String DEFAULT_SCHEMA = "public";
    public static final String CLIENT_KEY_DISCLAIMER_TEXT = "Securely store the client key and prevent exposing it to anyone!";

    public static final String DB_CONN_JSON_COLUMN_HOST = "host";
    public static final String DB_CONN_JSON_COLUMN_PORT = "port";
    public static final String DB_CONN_JSON_COLUMN_DB_NAME = "database_name";
    public static final String DB_CONN_JSON_COLUMN_USERNAME = "username";
    public static final String DB_CONN_JSON_COLUMN_PASSWORD = "password";
    public static final String DB_CONN_JSON_COLUMN_SSL_MODE = "ssl_mode";

    public static final String DB_QUERY_CHECK_REPLICATION_PRIVILEGE = """
                            SELECT rolreplication OR rolsuper AS can_replicate
                            FROM pg_roles
                            WHERE rolname = current_user
                            """;

    public static final String DB_QUERY_SHOW_WAL_LEVEL = """
                            SHOW wal_level
                            """;

    public static final String DB_QUERY_SHOW_MAX_REPLICATION_SLOTS = """
                            SHOW max_replication_slots
                            """;

    public static final String DB_QUERY_SHOW_MAX_WAL_SENDERS = """
                            SHOW max_wal_senders
                            """;

    public static final String DB_QUERY_CHECK_SELECT_PRIVILEGE = """
            SELECT
                to_regclass(?) IS NOT NULL AS table_exists,
                has_schema_privilege(
                    current_user, ?, 'USAGE'
                ) AS can_use_schema,
                CASE
                    WHEN to_regclass(?) IS NULL THEN FALSE
                    ELSE has_table_privilege(
                        current_user, to_regclass(?), 'SELECT'
                    )
                END AS can_select
            """;

    public static final String DB_QUERY_CHECK_INSERT_PRIVILEGE =  """
                SELECT
                    to_regclass(?) IS NOT NULL AS table_exists,
                    has_schema_privilege(
                        current_user, ?, 'USAGE'
                    ) AS can_use_schema,
                    CASE
                        WHEN to_regclass(?) IS NULL THEN FALSE
                        ELSE has_table_privilege(
                            current_user, to_regclass(?), 'INSERT'
                        )
                    END AS can_insert
                """;

    public static final String DB_QUERY_CHECK_PUBLICATION_TABLES = """
                SELECT EXISTS (
                    SELECT 1
                    FROM pg_publication_tables
                    WHERE pubname = ?
                      AND schemaname = ?
                      AND tablename = ?
                ) AS table_is_published
                """;

    public static final String DB_QUERY_CHECK_CREATE_PUBLICATION_PRIVILEGE = """
                SELECT has_database_privilege(
                    current_user, current_database(), 'CREATE'
                ) AS can_create_publication
                """;

    public static final String DB_QUERY_CHECK_TABLE_OWNERSHIP =  """
                SELECT pg_has_role(
                    current_user, c.relowner, 'MEMBER'
                ) AS can_manage_table
                FROM pg_class c
                JOIN pg_namespace n ON n.oid = c.relnamespace
                WHERE n.nspname = ?
                  AND c.relname = ?
                  AND c.relkind IN ('r', 'p')
                """;

    public static final int QUERY_TIMEOUT_SECONDS = 5;
    public static final String DEFAULT_PLAN_NAME_TEMPLATE = "%s - %s plan";

}
