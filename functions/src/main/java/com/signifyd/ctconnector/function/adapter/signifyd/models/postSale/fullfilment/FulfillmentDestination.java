package com.signifyd.ctconnector.function.adapter.signifyd.models.postSale.fullfilment;

import com.signifyd.ctconnector.function.adapter.signifyd.models.Address;
import lombok.Builder;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "fullName",
    "organization",
    "email",
    "address",
    "confirmationPhone",
})

@Data
@Builder
public class FulfillmentDestination {

    @JsonProperty("fullName")
    public String fullName;
    @JsonProperty("organization")
    public String organization;
    @JsonProperty("email")
    public String email;
    @JsonProperty("confirmationPhone")
    public String confirmationPhone;
    @JsonProperty("address")
    public Address address;
}
