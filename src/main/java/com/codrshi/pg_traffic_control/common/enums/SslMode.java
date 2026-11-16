package com.codrshi.pg_traffic_control.common.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SslMode {
    DISABLE("disable"),
    ALLOW("allow"),
    PREFER("prefer"),
    REQUIRE("require"),
    VERIFY_CA("verify-ca"),
    VERIFY_FULL("verify-full");

    @JsonValue
    private final String value;
}
