package com.codrshi.pg_traffic_control.dto;

import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;

public record PreflightInspectionFailure(PfiFailureCode code,
                                         String message) { }
