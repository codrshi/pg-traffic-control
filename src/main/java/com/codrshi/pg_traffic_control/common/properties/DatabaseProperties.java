package com.codrshi.pg_traffic_control.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.datasource")
public record DatabaseProperties(
        String url,
        String username,
        String password
) {
}
