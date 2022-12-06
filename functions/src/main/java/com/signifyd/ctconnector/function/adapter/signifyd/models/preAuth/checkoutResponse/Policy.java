package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

enum Status {
    EVALUATED_TRUE, EVALUATED_FALSE, SKIPPED
}

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Policy {
    @JsonProperty(value = "name", required = true)
    public String name;
    @JsonProperty(value = "status", required = true)
    public Status status;
    @JsonProperty(value = "action", required = true)
    public Action action;
    @JsonProperty(value = "reason", required = true)
    public String reason;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}