package com.signifyd.connector.springboot.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SnsSubscriptionConfirmationMessage extends SnsMessage {
    @JsonProperty("Token")
    private String token;
    @JsonProperty("SubscribeURL")
    private String subscribeURL;
}
