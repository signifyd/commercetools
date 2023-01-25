package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class Refund implements Serializable {
    @JsonProperty("method")
    private String method;
    @JsonProperty("amount")
    private Double amount;
    @JsonProperty("currency")
    private String currency;
}