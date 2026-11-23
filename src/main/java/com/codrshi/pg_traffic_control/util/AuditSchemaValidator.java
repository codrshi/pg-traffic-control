package com.codrshi.pg_traffic_control.util;

import java.util.*;

public class AuditSchemaValidator {
    private Map<String, AuditColumn> auditColumnMap;
    private static final List<AuditColumn> auditColumns;

    static {
        auditColumns = List.of(
                new AuditColumn("metadata", "YES", "jsonb"),
                new AuditColumn("captured_at", "NO", "timestamp with time zone"),
                new AuditColumn("received_at", "NO", "timestamp with time zone"),
                new AuditColumn("before_data", "YES", "jsonb"),
                new AuditColumn("after_data", "YES", "jsonb"),
                new AuditColumn("record_key", "NO", "jsonb"),
                new AuditColumn("flight_plan_id", "NO", "text"),
                new AuditColumn("operation", "NO", "character varying"),
                new AuditColumn("event_id", "NO", "text"),
                new AuditColumn("captured_table", "NO", "character varying")
        );
    }

    private AuditSchemaValidator() {}

    public static AuditSchemaValidator getInstance() {

        AuditSchemaValidator validator = new AuditSchemaValidator();
        validator.auditColumnMap = new HashMap<>();

        return validator;
    }

    public void add(String columnName, String isNullable, String dataType) {
        if(columnName == null){
            return;
        }

        auditColumnMap.put(columnName, new AuditColumn(columnName, isNullable, dataType));
    }

    public List<String> validate() {
        return auditColumns.stream()
                .filter(expected -> !Objects.equals(expected, auditColumnMap.get(expected.columnName())))
                .map(AuditColumn::columnName)
                .toList();
    }

    private record AuditColumn(String columnName, String isNullable, String dataType) {}
}
