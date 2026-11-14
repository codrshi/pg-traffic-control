package com.codrshi.pg_traffic_control.repository;

import com.codrshi.pg_traffic_control.entity.FlightOperator;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FlightOperatorRepository extends JpaRepository<FlightOperator, UUID> {

}
