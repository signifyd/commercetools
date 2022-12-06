package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
public class SignifydError {
    @JsonProperty("traceId")
    private String traceId;
    @JsonProperty("errors")
    private Map<String, String[]> errors;
}
