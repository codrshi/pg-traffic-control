package com.codrshi.pg_traffic_control.common.enums;

public enum FlightPlanStatus {
    DRAFT,
    INSPECTING,
    CLEARED,
    GROUNDED,
    AIRBORNE,
    PAUSED,
    ABORTED,
    DECOMMISSIONED
}

//    -- draft: plan is just created
//    -- inspecting: the platform is running pre-flight inspection to validate plan (origin db, destination db, permission, tables)
//    -- cleared: plan is ready to use (preflight inspection successful)
//    -- grounded: plan failed in validation (preflight inspection failed)
//    -- airborne: plan is currently executing in CDC engine & monitored by ATC
//    -- paused:  operator/platform has paused the plan execution
//    -- aborted: plan has failed to execute or failed mid-execution
//    -- decommissioned: permanently deleted