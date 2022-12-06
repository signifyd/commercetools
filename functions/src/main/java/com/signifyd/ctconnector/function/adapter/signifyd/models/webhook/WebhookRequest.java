package com.signifyd.ctconnector.function.adapter.signifyd.models.webhook;

import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WebhookRequest {
    private String signifydCheckpoint;
    private DecisionResponse decisionResponse;
}
