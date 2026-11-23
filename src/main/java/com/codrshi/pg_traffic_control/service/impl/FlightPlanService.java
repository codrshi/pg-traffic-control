package com.codrshi.pg_traffic_control.service.impl;

import com.codrshi.pg_traffic_control.common.Constant;
import com.codrshi.pg_traffic_control.common.enums.DbConnectionRole;
import com.codrshi.pg_traffic_control.common.enums.FlightOperatorStatus;
import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import com.codrshi.pg_traffic_control.dto.SchemaTable;
import com.codrshi.pg_traffic_control.dto.request.CreateFlightPlanRequest;
import com.codrshi.pg_traffic_control.dto.response.CreateFlightPlanResponse;
import com.codrshi.pg_traffic_control.entity.CaptureRunway;
import com.codrshi.pg_traffic_control.entity.DatabaseConnection;
import com.codrshi.pg_traffic_control.entity.FlightOperator;
import com.codrshi.pg_traffic_control.entity.FlightPlan;
import com.codrshi.pg_traffic_control.exception.ResourceNotFoundException;
import com.codrshi.pg_traffic_control.exception.UnprocessableContentException;
import com.codrshi.pg_traffic_control.repository.FlightOperatorRepository;
import com.codrshi.pg_traffic_control.repository.FlightPlanRepository;
import com.codrshi.pg_traffic_control.service.IFlightPlanService;
import com.codrshi.pg_traffic_control.util.DbUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class FlightPlanService implements IFlightPlanService {

    @Autowired
    private FlightOperatorRepository flightOperatorRepository;

    @Autowired
    private FlightPlanRepository flightPlanRepository;

    @Override
    @Transactional
    public CreateFlightPlanResponse createPlan(String clientKey, CreateFlightPlanRequest request) {

        log.info("Received flight plan creation request with name = {} by operator = {}", request.name(), clientKey);

        FlightOperator flightOperator = retrieveAndValidateOperator(clientKey);
        log.debug("Resolved flight operator details: {}", flightOperator);

        SchemaTable defaultAuditSchemaTable = DbUtil.resolveSchema(request.defaultAuditTable());

        Map<String, Object> originConnDetails = DbUtil.resolveConnectionDetails(request.originConnectionUrl());
        DatabaseConnection originDatabaseConnection = DatabaseConnection.builder()
                .connectionRole(DbConnectionRole.ORIGIN)
                .connectionDetails(originConnDetails)
                .defaultAuditSchema(defaultAuditSchemaTable.schema())
                .defaultAuditTable(defaultAuditSchemaTable.table())
                .build();

        Map<String, Object> destinationConnDetails = DbUtil.resolveConnectionDetails(request.destinationConnectionUrl());
        DatabaseConnection destinationDatabaseConnection = DatabaseConnection.builder()
                .connectionRole(DbConnectionRole.DESTINATION)
                .connectionDetails(destinationConnDetails)
                .defaultAuditSchema(defaultAuditSchemaTable.schema())
                .defaultAuditTable(defaultAuditSchemaTable.table())
                .build();

        log.debug("Successfully resolved origin and destination connection details: {}, {}", originConnDetails, destinationConnDetails);

        List<CaptureRunway> captureRunaways = createRunaways(request.runaways(), request.defaultAuditTable(), request.publicationName());
        String planName = resolvePlanName(request.name(), originConnDetails.get(Constant.DB_CONN_JSON_COLUMN_DB_NAME), destinationConnDetails.get(Constant.DB_CONN_JSON_COLUMN_DB_NAME));

        FlightPlan flightPlan = FlightPlan.builder()
                .name(planName)
                .flightOperator(flightOperator)
                .status(FlightPlanStatus.DRAFT)
                .databaseConnections(List.of(originDatabaseConnection, destinationDatabaseConnection))
                .captureRunaways(captureRunaways)
                .build();

        FlightPlan savedFlightPlan = flightPlanRepository.saveAndFlush(flightPlan);

        return buildResponse(savedFlightPlan);
    }

    private FlightOperator retrieveAndValidateOperator(String clientKey) {
        FlightOperator flightOperator = flightOperatorRepository.findByClientKey(clientKey)
                .orElseThrow(() -> new ResourceNotFoundException("Flight operator not found."));

        if(!FlightOperatorStatus.ACTIVE.equals(flightOperator.getStatus())) {
            throw new IllegalStateException("Cannot create a Flight Plan for an inactive operator.");
        }

        return flightOperator;
    }

    private static String resolvePlanName(String planName, Object originDbName, Object destinationDbName) {
        return Optional.ofNullable(planName)
                .orElse(String.format(Constant.DEFAULT_PLAN_NAME_TEMPLATE, originDbName, destinationDbName));
    }

    private static List<CaptureRunway> createRunaways(List<CreateFlightPlanRequest.Runaway> runaways, String defaultAuditTable, String publicationName) {
        List<CaptureRunway> captureRunways = new ArrayList<>();

        for(CreateFlightPlanRequest.Runaway runaway: runaways) {
            String departure = runaway.departure();
            String arrival = Optional.ofNullable(runaway.arrival()).orElse(defaultAuditTable);

            if(arrival == null || departure == null) {
                continue;
            }

            SchemaTable departureSchemaTable = DbUtil.resolveSchema(departure);
            SchemaTable arrivalSchemaTable = DbUtil.resolveSchema(arrival);

            CaptureRunway captureRunway = CaptureRunway.builder()
                    .sourceSchema(departureSchemaTable.schema())
                    .sourceTable(departureSchemaTable.table())
                    .publicationName(publicationName)
                    .auditSchema(arrivalSchemaTable.schema())
                    .auditTable(arrivalSchemaTable.table())
                    .enabled(true)
                    .build();

            captureRunways.add(captureRunway);
        }

        if(captureRunways.isEmpty()) {
            throw new UnprocessableContentException("No runaways (tables) to listen.");
        }

        return captureRunways;
    }

    private static CreateFlightPlanResponse buildResponse(FlightPlan flightPlan) {
        return CreateFlightPlanResponse.builder()
                .planId(flightPlan.getId())
                .planName(flightPlan.getName())
                .status(flightPlan.getStatus())
                .version(flightPlan.getVersion())
                .createdAt(flightPlan.getCreatedAt())
                .updatedAt(flightPlan.getUpdatedAt())
                .build();
    }
}
