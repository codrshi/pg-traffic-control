package com.codrshi.pg_traffic_control.entity;

import com.codrshi.pg_traffic_control.enums.FlightPlanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "flight_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightPlan {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "flight_operator_id", nullable = false)
    private FlightOperator flightOperator;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private FlightPlanStatus status = FlightPlanStatus.DRAFT;

    @Version
    @Column(name = "version", nullable = false)
    private Integer version = 1;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @OneToMany(
            mappedBy = "flightPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<DatabaseConnection> databaseConnections = new ArrayList<>();

    @OneToMany(
            mappedBy = "flightPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<CaptureRunways> captureTargets = new ArrayList<>();

    public void addDatabaseConnection(DatabaseConnection connection) {
        databaseConnections.add(connection);
        connection.setFlightPlan(this);
    }

    public void removeDatabaseConnection(DatabaseConnection connection) {
        databaseConnections.remove(connection);
        connection.setFlightPlan(null);
    }

    public void addCaptureTarget(CaptureRunways target) {
        captureTargets.add(target);
        target.setFlightPlan(this);
    }

    public void removeCaptureTarget(CaptureRunways target) {
        captureTargets.remove(target);
        target.setFlightPlan(null);
    }
}
