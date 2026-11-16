package com.codrshi.pg_traffic_control.service.impl;

import com.codrshi.pg_traffic_control.dto.request.CreateOperatorRequest;
import com.codrshi.pg_traffic_control.dto.response.CreateOperatorResponse;
import com.codrshi.pg_traffic_control.entity.FlightOperator;
import com.codrshi.pg_traffic_control.common.enums.FlightOperatorStatus;
import com.codrshi.pg_traffic_control.repository.FlightOperatorRepository;
import com.codrshi.pg_traffic_control.service.IFlightOperatorService;
import com.codrshi.pg_traffic_control.util.ClientKeyGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class FlightOperatorService implements IFlightOperatorService {

    @Autowired
    private FlightOperatorRepository flightOperatorRepository;

    @Override
    public CreateOperatorResponse registerOperator(CreateOperatorRequest request) {
        log.info("Received request to register operator: {}", request);

        String clientKey = ClientKeyGenerator.generate();
        log.debug("Client key generated for {}", request.name());

        FlightOperator flightOperator = FlightOperator.builder()
                .name(request.name())
                .clientKey(clientKey)
                .status(FlightOperatorStatus.ACTIVE)
                .build();

        FlightOperator savedOperator = flightOperatorRepository.save(flightOperator);
        log.info("Operator registered successfully with id = {} at {}", savedOperator.getId(), savedOperator.getCreatedAt());

        return new CreateOperatorResponse(clientKey);
    }
}
