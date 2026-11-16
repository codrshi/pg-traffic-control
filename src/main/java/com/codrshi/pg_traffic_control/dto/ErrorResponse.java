package com.codrshi.pg_traffic_control.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ErrorResponse(Integer errorCode, String message, String path, String traceId, LocalDateTime timestamp)
{
}
