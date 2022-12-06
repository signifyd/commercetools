package com.signifyd.ctconnector.function.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Credentials {
    @JsonProperty(value = "SIGNIFYD",required = true)
    private SignifydCredential signifyd;
    @JsonProperty(value = "COMMERCETOOLS",required = true)
    private CommercetoolsCredential commercetools;
}
