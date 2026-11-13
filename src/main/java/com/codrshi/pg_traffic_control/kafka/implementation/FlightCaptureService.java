package com.codrshi.pg_traffic_control.kafka.implementation;

import com.codrshi.pg_traffic_control.kafka.IFlightCaptureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class FlightCaptureService implements IFlightCaptureService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void capture(String message) {
        kafkaTemplate.send("inventory-items-audit", message);
    }
}
