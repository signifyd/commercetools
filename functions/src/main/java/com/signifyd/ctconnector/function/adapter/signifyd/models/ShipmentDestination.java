package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "fullName",
        "organization",
        "email",
        "address"
})
@Builder
@Data
public class ShipmentDestination {
    @JsonProperty("fullName")
    public String fullName;
    @JsonProperty("organization")
    public String organization;
    @JsonProperty("email")
    public Object email;
    @JsonProperty("address")
    public Address address;
}
