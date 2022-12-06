package com.signifyd.connector.springboot.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class SnsNotificationMessage extends SnsMessage {
    @JsonProperty("Subject")
    private String subject;
    @JsonProperty("UnsubscribeURL")
    private String unsubscribeURL;
}
