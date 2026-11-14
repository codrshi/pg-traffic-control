package com.codrshi.pg_traffic_control.repository;

import com.codrshi.pg_traffic_control.entity.DatabaseConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DatabaseConnectionRepository extends JpaRepository<DatabaseConnection, UUID> {
}
