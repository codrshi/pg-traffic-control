package com.codrshi.pg_traffic_control.dto;

import com.codrshi.pg_traffic_control.entity.CaptureRunway;
import com.codrshi.pg_traffic_control.entity.DatabaseConnection;

import java.util.List;
import java.util.UUID;

public record PreflightInspectionContext(UUID planId, List<CaptureRunway> captureRunways, List<DatabaseConnection> databaseConnections) {
}
