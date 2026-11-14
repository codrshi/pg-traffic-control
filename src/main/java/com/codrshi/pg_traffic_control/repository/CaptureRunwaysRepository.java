package com.codrshi.pg_traffic_control.repository;

import com.codrshi.pg_traffic_control.entity.CaptureRunways;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CaptureRunwaysRepository extends JpaRepository<CaptureRunways, UUID> {
}
