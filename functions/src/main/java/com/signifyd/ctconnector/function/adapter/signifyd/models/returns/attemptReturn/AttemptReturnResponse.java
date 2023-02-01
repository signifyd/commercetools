package com.signifyd.ctconnector.function.adapter.signifyd.models.returns.attemptReturn;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Coverage;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Decision;
import lombok.Data;

@Data
public class AttemptReturnResponse {
  @JsonProperty("orderId")
  private String orderId;
  @JsonProperty("returnId")
  private String returnId;
  @JsonProperty("decision")
  private Decision decision;
  @JsonProperty("coverage")
  private Coverage coverage;
}
