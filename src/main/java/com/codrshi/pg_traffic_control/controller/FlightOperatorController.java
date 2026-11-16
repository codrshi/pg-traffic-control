package com.codrshi.pg_traffic_control.controller;

import com.codrshi.pg_traffic_control.dto.request.CreateOperatorRequest;
import com.codrshi.pg_traffic_control.dto.response.CreateOperatorResponse;
import com.codrshi.pg_traffic_control.service.IFlightOperatorService;
import com.codrshi.pg_traffic_control.service.impl.FlightOperatorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flight/operator")
public class FlightOperatorController {

    @Autowired
    private IFlightOperatorService flightOperatorService;

    @PostMapping("/v1/create")
    public ResponseEntity<CreateOperatorResponse> registerOperator(@Valid @RequestBody CreateOperatorRequest request) {
        CreateOperatorResponse response = flightOperatorService.registerOperator(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
