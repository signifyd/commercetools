package com.signifyd.ctconnector.function.adapter.signifyd.enums;

public enum ErrorCodes4XX {
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    CONFLICT(409);

    private final int value;

    ErrorCodes4XX(final int newValue) {
        this.value = newValue;
    }

    public static boolean is4xxError(int statusCode) {
        for (ErrorCodes4XX errorCode : ErrorCodes4XX.values()) {
            if (statusCode == errorCode.value) {
                return true;
            }
        }
        return false;
    }

    public int getValue() { return value; }
}
