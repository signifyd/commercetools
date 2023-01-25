package com.signifyd.ctconnector.function.adapter.signifyd.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
@Data
public class Initiator implements Serializable {
    @JsonProperty("employeeEmail")
    private String employeeEmail;
    @JsonProperty("employeeId")
    private String employeeId;
}