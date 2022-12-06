package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Builder;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "username",
        "createdDate",
        "email",
        "phone"
})
@Data
@Builder
public class UserAccount {

    @JsonProperty("username")
    public String username;
    @JsonProperty("createdDate")
    public String createdDate;
    @JsonProperty("email")
    public String email;
    @JsonProperty("phone")
    public String phone;
}
