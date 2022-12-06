package com.signifyd.ctconnector.function.adapter.signifyd.exception;

import com.signifyd.ctconnector.function.adapter.signifyd.models.ErrorResponse;

public class Signifyd4xxException extends Exception {
    private final ErrorResponse response;

    public Signifyd4xxException(ErrorResponse response, int errorCode) {
        super(String.format("Received error %s with traceId %s", errorCode, response.getTraceId()));
        this.response = response;
    }

    public ErrorResponse getResponse() {
        return response;
    }
}
