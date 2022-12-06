package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "createdAt",
        "checkpointAction",
        "checkpointActionReason",
        "checkpointActionPolicy",
        "policies",
        "score"
})
@Data
public class Decision {
    @JsonProperty(value = "createdAt", required = true)
    public String createdAt;
    @JsonProperty(value = "checkpointAction", required = true)
    public Action checkpointAction;
    @JsonProperty("checkpointActionReason")
    public String checkpointActionReason;
    @JsonProperty("checkpointActionPolicy")
    public String checkpointActionPolicy;
    @JsonProperty("policies")
    public Policies policies;
    @JsonProperty("score")
    public int score;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
}