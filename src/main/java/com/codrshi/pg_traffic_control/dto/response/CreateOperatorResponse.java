package com.codrshi.pg_traffic_control.dto.response;

import com.codrshi.pg_traffic_control.common.Constant;

public record CreateOperatorResponse(
        String clientKey,
        String disclaimerText
) {
    public CreateOperatorResponse(String clientKey) {
        this(clientKey, Constant.CLIENT_KEY_DISCLAIMER_TEXT);
    }
}
