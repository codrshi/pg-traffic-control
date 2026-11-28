package com.codrshi.pg_traffic_control.config;

import com.codrshi.pg_traffic_control.common.properties.KafkaProperties;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic flightTopicV0(KafkaProperties kafkaProperties) {
        KafkaProperties.TopicConfig flightTopicV0 = kafkaProperties.topics().get("pgtc-flight-topic-v0");

        return TopicBuilder.name(flightTopicV0.name())
                .partitions(flightTopicV0.partition())
                .replicas(flightTopicV0.replicas())
                .build();
    }

}
