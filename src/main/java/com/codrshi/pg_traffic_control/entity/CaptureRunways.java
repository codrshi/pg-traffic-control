package com.codrshi.pg_traffic_control.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "capture_runways",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_flight_plan_source_table",
                        columnNames = {"flight_plan_id", "source_schema", "source_table"}
                )
        },
        indexes = {
                @Index(name = "idx_capture_runways_flight_plan", columnList = "flight_plan_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaptureRunways {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_plan_id", nullable = false)
    private FlightPlan flightPlan;

    @Column(name = "source_schema", nullable = false, length = 150)
    private String sourceSchema;

    @Column(name = "source_table", nullable = false, length = 150)
    private String sourceTable;

    @Column(name = "audit_schema", length = 150)
    private String auditSchema;

    @Column(name = "audit_table", length = 150)
    private String auditTable;

    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
