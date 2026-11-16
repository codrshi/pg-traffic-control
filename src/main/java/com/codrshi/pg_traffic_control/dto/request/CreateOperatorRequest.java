package com.codrshi.pg_traffic_control.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateOperatorRequest(
        @NotBlank(message = "Operator name is required and cannot be blank.")
        @Size(max = 100, message = "Operator name must not exceed 100 characters.")
        String name
) {}
