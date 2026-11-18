package com.codrshi.pg_traffic_control.controller;

import com.codrshi.pg_traffic_control.dto.request.CreateFlightPlanRequest;
import com.codrshi.pg_traffic_control.dto.request.CreateOperatorRequest;
import com.codrshi.pg_traffic_control.dto.response.CreateFlightPlanResponse;
import com.codrshi.pg_traffic_control.dto.response.CreateOperatorResponse;
import com.codrshi.pg_traffic_control.service.IFlightOperatorService;
import com.codrshi.pg_traffic_control.service.IFlightPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/flight/plan")
@Validated
public class FlightPlanController {
    @Autowired
    private IFlightPlanService flightPlanService;

    @PostMapping("/v1/create")
    public ResponseEntity<CreateFlightPlanResponse> registerPlan(@RequestParam(name = "client_key") @NotBlank(message = "clientKey must not be blank") String clientKey,
                                                                 @Valid @RequestBody CreateFlightPlanRequest request) {
        CreateFlightPlanResponse response = flightPlanService.createPlan(clientKey, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
