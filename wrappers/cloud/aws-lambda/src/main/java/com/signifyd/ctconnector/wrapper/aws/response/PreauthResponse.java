package com.signifyd.ctconnector.wrapper.aws.response;

import lombok.Data;

@Data
public class PreauthResponse {
    private int statusCode;
    private String body;
}
