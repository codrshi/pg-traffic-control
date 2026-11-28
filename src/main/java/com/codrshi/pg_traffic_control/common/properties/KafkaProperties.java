package com.codrshi.pg_traffic_control.common.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

@ConfigurationProperties(prefix = "app.kafka")
public record KafkaProperties(Map<String, TopicConfig> topics) {

    public record TopicConfig(String name, int partition, short replicas) {}
}
