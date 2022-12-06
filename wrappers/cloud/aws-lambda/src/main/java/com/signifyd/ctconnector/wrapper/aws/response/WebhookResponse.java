package com.signifyd.ctconnector.wrapper.aws.response;

import lombok.Data;

@Data
public class WebhookResponse {
    private int statusCode;
    private String body;

    public WebhookResponse(int statusCode, String body){
        this.statusCode = statusCode;
        this.body = body;
    }
}
