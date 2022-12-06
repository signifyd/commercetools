package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutRequestDraft;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Builder
@Data
public class CardInstallments implements Serializable {
    @JsonProperty("interval")
    private String interval;
    @JsonProperty("count")
    private int count;
    @JsonProperty("totalValue")
    private Double totalValue;
    @JsonProperty("installmentValue")
    private Double installmentValue;
}
