package com.signifyd.ctconnector.function.adapter.signifyd.enums;

public enum ErrorCodes5XX {
    INTERNAL_SERVER_ERROR(500),
    SERVICE_UNAVAILABLE(503),
    GATEWAY_TIMEOUT(504);

    private final int value;

    ErrorCodes5XX(final int newValue) {
        this.value = newValue;
    }

    public static boolean is5xxError(int statusCode) {
        for (ErrorCodes5XX errorCode : ErrorCodes5XX.values()) {
            if (statusCode == errorCode.value) {
                return true;
            }
        }
        return false;
    }

    public int getValue() { return value; }
}
