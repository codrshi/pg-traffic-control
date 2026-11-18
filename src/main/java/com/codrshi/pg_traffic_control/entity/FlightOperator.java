package com.codrshi.pg_traffic_control.entity;

import com.codrshi.pg_traffic_control.common.enums.FlightOperatorStatus;
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
@Table(name = "flight_operators")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlightOperator {
    @Id
    @UuidGenerator(style = UuidGenerator.Style.VERSION_7)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "client_key", nullable = false, unique = true, length = 100)
    private String clientKey;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private FlightOperatorStatus status = FlightOperatorStatus.ACTIVE;

    @CreationTimestamp(source = SourceType.DB)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp(source = SourceType.DB)
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(
            mappedBy = "flightOperator",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<FlightPlan> flightPlans = new ArrayList<>();

    public void addFlightPlan(FlightPlan flightPlan) {
        flightPlans.add(flightPlan);
        flightPlan.setFlightOperator(this);
    }

    public void removeFlightPlan(FlightPlan flightPlan) {
        flightPlans.remove(flightPlan);
        flightPlan.setFlightOperator(null);
    }
}
