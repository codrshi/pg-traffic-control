package com.codrshi.pg_traffic_control.kafka;

import org.apache.kafka.connect.source.SourceRecord;

import java.util.UUID;

public interface IFlightCaptureService {

    void capture(SourceRecord record, UUID flightPlanId);
}
