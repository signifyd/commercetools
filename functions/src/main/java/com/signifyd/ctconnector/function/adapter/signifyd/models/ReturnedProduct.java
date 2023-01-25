package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class ReturnedProduct implements Serializable {
    @JsonProperty("reason")
    private String reason;
    @JsonProperty("itemName")
    private String itemName;
    @JsonProperty("itemPrice")
    private Double itemPrice;
    @JsonProperty("itemQuantity")
    private Integer itemQuantity;
    @JsonProperty("itemIsDigital")
    private Boolean itemIsDigital;
    @JsonProperty("itemCategory")
    private String itemCategory;
    @JsonProperty("itemSubCategory")
    private String itemSubCategory;
    @JsonProperty("itemId")
    private String itemId;
}