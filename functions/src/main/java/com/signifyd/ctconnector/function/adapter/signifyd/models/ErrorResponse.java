package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class ErrorResponse {
    @JsonProperty("messages")
    private String[] messages;
    @JsonProperty("traceId")
    private String traceId;
    @JsonProperty("errors")
    private Map<String, String[]> errors;
}
