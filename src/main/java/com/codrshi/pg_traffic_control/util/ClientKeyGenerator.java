package com.codrshi.pg_traffic_control.util;

import java.security.SecureRandom;
import java.util.HexFormat;

public class ClientKeyGenerator {
    private static final SecureRandom RANDOM = new SecureRandom();

    public static String generate() {
        byte[] bytes = new byte[16];
        RANDOM.nextBytes(bytes);

        return "pg-operator_" + HexFormat.of().formatHex(bytes);
    }
}
