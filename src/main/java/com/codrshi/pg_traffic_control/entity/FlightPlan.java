package com.codrshi.pg_traffic_control.entity;

import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SourceType;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
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

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
    private List<CaptureRunway> captureRunaways = new ArrayList<>();

    public void addDatabaseConnection(DatabaseConnection connection) {
        databaseConnections.add(connection);
        connection.setFlightPlan(this);
    }

    public void removeDatabaseConnection(DatabaseConnection connection) {
        databaseConnections.remove(connection);
        connection.setFlightPlan(null);
    }

    public void addCaptureTarget(CaptureRunway target) {
        captureRunaways.add(target);
        target.setFlightPlan(this);
    }

    public void removeCaptureTarget(CaptureRunway target) {
        captureRunaways.remove(target);
        target.setFlightPlan(null);
    }

    public static class FlightPlanBuilder {
        public FlightPlan build() {
            FlightPlan plan = new FlightPlan(
                    id,
                    flightOperator,
                    name,
                    status != null ? status : FlightPlanStatus.DRAFT,
                    version != null ? version : 1,
                    createdAt,
                    updatedAt,
                    new ArrayList<>(),
                    new ArrayList<>()
            );

            if (this.databaseConnections != null) {
                this.databaseConnections.forEach(plan::addDatabaseConnection);
            }
            if (this.captureRunaways != null) {
                this.captureRunaways.forEach(plan::addCaptureTarget);
            }
            return plan;
        }
    }
}
