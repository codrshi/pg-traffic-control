package com.codrshi.pg_traffic_control.entity;


import com.codrshi.pg_traffic_control.common.enums.DatabaseType;
import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table( name = "database_connections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_flight_plan_connection_role",
                        columnNames = {"flight_plan_id", "connection_role"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DatabaseConnection {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_plan_id", nullable = false)
    private FlightPlan flightPlan;

    @Enumerated(EnumType.STRING)
    @Column(name = "connection_role", nullable = false, length = 20)
    private DbConnectionRole connectionRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "database_type", nullable = false, length = 30)
    private DatabaseType databaseType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "connection_details", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> connectionDetails = new HashMap<>();

    @Column(name = "default_audit_schema", length = 150)
    private String defaultAuditSchema;

    @Column(name = "default_audit_table", length = 150)
    private String defaultAuditTable;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

}
