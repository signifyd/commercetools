package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "clientIpAddress",
        "sessionId",
        "fingerprint"
})
@Data
@Builder
public class Device {

    @JsonProperty("clientIpAddress")
    public String clientIpAddress;
    @JsonProperty("sessionId")
    public String sessionId;
    @JsonProperty("fingerprint")
    public Fingerprint fingerprint;

}
