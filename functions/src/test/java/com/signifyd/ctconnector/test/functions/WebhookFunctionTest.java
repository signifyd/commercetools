package com.signifyd.ctconnector.test.functions;

import com.commercetools.api.models.order.OrderImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signifyd.ctconnector.function.WebhookFunction;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Decision;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.ScaEvaluation;
import com.signifyd.ctconnector.function.adapter.signifyd.models.webhook.WebhookRequest;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ActionType;
import com.signifyd.ctconnector.function.config.model.DecisionActionConfig;
import com.signifyd.ctconnector.function.config.model.DecisionActionConfigs;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class WebhookFunctionTest {

    @Mock
    private ConfigReader configReader;

    @Mock
    private CommercetoolsClient commercetoolsClient;

    private WebhookFunction webhookFunction;

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setupAll(){
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    @BeforeEach
    public void setupEach() {
        webhookFunction = new WebhookFunction(configReader, commercetoolsClient);

    }

    @Test
    public void WHEN_CheckpointActionIsAccept_AND_ExecutionTypeIsActive_THEN_ReturnSuccess() {
        // given
        DecisionResponse decisionResponse = new DecisionResponse();
        Decision decision = new Decision();
        decision.setCheckpointAction(Action.ACCEPT);
        decisionResponse.setDecision(decision);
        decisionResponse.setOrderId("orderId");
        OrderImpl order = new OrderImpl();
        order.setCountry("country");
        WebhookRequest webhookRequest = WebhookRequest.builder()
                .decisionResponse(decisionResponse)
                .signifydCheckpoint("PREAUTH")
                .build();
        // when
        when(this.commercetoolsClient.getOrderById(decisionResponse.getOrderId())).thenReturn(order);
        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.ACTIVE);
        DecisionActionConfigs decisionActionConfigs = new DecisionActionConfigs();
        DecisionActionConfig decisionActionConfig = new DecisionActionConfig();
        decisionActionConfig.setActionType(ActionType.NONE);
        decisionActionConfigs.setAccept(decisionActionConfig);
        when(this.configReader.getDecisionActions(order.getCountry())).thenReturn(decisionActionConfigs);
        // then

        String response = webhookFunction.apply(webhookRequest);
        assertThat(response).isNotNull();
    }

    @SneakyThrows
    @Test
    public void WHEN_CheckpointActionIsAccept_AND_ExecutionTypeIsNotActive_THEN_ReturnSuccess() {
        // given
        DecisionResponse decisionResponse = new DecisionResponse();
        Decision decision = new Decision();
        decision.setCheckpointAction(Action.ACCEPT);
        decisionResponse.setDecision(decision);
        decisionResponse.setOrderId("orderId");
        decisionResponse.setSignifydId("signifydId");
        String scaEvaluationJson = "{\n" +
                "    \"outcome\": \"REQUEST_EXEMPTION\",\n" +
                "    \"exemptionDetails\": {\n" +
                "      \"exemption\": \"TRA\",\n" +
                "      \"placement\": \"AUTHORIZATION\"\n" +
                "    },\n" +
                "    \"exclusionDetails\":{\n" +
                "      \"exclusion\": \"ONE_LEG_OUT\"\n" +
                "    }\n" +
                "  }";
        ScaEvaluation scaEvaluation = objectMapper.readValue(scaEvaluationJson, ScaEvaluation.class);
        decisionResponse.setScaEvaluation(scaEvaluation);
        OrderImpl order = new OrderImpl();
        order.setCountry("country");
        WebhookRequest webhookRequest = WebhookRequest.builder()
                .decisionResponse(decisionResponse)
                .signifydCheckpoint("PREAUTH")
                .build();
        // when
        when(this.commercetoolsClient.getOrderById(decisionResponse.getOrderId())).thenReturn(order);
        when(this.configReader.getExecutionMode()).thenReturn(ExecutionMode.PASSIVE);
        // then

        String response = webhookFunction.apply(webhookRequest);
        assertThat(response).isNotNull();
    }
}
