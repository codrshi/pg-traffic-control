package com.codrshi.pg_traffic_control.repository;

import com.codrshi.pg_traffic_control.entity.CaptureRunway;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CaptureRunwayRepository extends JpaRepository<CaptureRunway, UUID> {
}
