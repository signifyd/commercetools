package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({"itemName", "itemPrice", "itemQuantity", "itemIsDigital", "itemCategory", "itemSubCategory", "itemId"})
@Builder
@Data
public class Product implements Serializable {

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
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();
    private final static long serialVersionUID = 4216978379761063339L;

}