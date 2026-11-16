package com.codrshi.pg_traffic_control.repository;

import com.codrshi.pg_traffic_control.entity.FlightPlan;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FlightPlanRepository extends JpaRepository<FlightPlan, UUID> {
    @EntityGraph(attributePaths = {
            "databaseConnections",
            "captureRunaways"
    })
    Optional<FlightPlan> findByIdAndFlightOperatorId(
            UUID id,
            UUID flightOperatorId
    );

    @EntityGraph(attributePaths = {
            "databaseConnections",
            "captureRunaways"
    })
    List<FlightPlan> findByFlightOperatorId(
            UUID flightOperatorId
    );
}
