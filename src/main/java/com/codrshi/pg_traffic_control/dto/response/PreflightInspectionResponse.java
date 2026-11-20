package com.codrshi.pg_traffic_control.dto.response;

import com.codrshi.pg_traffic_control.common.enums.FlightPlanStatus;
import com.codrshi.pg_traffic_control.common.enums.PfiFailureCode;
import com.codrshi.pg_traffic_control.dto.PreflightInspectionFailure;
import lombok.Builder;

import java.util.List;

@Builder
public record PreflightInspectionResponse(String planName,
                                          FlightPlanStatus status,
                                          List<PfiFailureCode> inspectionFailures )
{
}
