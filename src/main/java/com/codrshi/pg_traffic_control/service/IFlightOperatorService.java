package com.codrshi.pg_traffic_control.service;

import com.codrshi.pg_traffic_control.dto.request.CreateOperatorRequest;
import com.codrshi.pg_traffic_control.dto.response.CreateOperatorResponse;

public interface IFlightOperatorService {
    CreateOperatorResponse registerOperator(CreateOperatorRequest request);
}
