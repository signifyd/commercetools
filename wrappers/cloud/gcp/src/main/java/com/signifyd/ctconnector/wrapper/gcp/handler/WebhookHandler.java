package com.signifyd.ctconnector.wrapper.gcp.handler;

import ch.qos.logback.classic.Logger;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.signifyd.ctconnector.function.WebhookFunction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.webhook.WebhookRequest;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.constants.SignifydApi;
import com.signifyd.ctconnector.function.utils.SignifydWebhookValidator;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;

public class WebhookHandler implements HttpFunction {
    private static final WebhookFunction function = new WebhookFunction();
    private static final ConfigReader configReader = new ConfigReader();
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode())) {
            httpResponse.setStatusCode(200);
            return;
        }
        var rawBody = httpRequest.getReader().lines().collect(Collectors.joining());
        String signifydSecHmacSha256 = httpRequest.getFirstHeader(SignifydApi.SHA_256_HEADER).orElseThrow() + "=";
        if (!SignifydWebhookValidator.validateWebhook(signifydSecHmacSha256, rawBody, configReader.getSignifydTeamAPIKey())) {
            throw new IllegalArgumentException("Webhook is not valid.");
        }
        try {
            var response = objectMapper.readValue(rawBody, DecisionResponse.class);
            String signifydCheckpoint = httpRequest.getFirstHeader(SignifydApi.CHECK_POINT_HEADER).orElseThrow();
            WebhookRequest webhookRequest =
                    WebhookRequest.builder()
                            .decisionResponse(response)
                            .signifydCheckpoint(signifydCheckpoint)
                            .build();
            logger.info("Signifyd Webhook payload: {}", objectMapper.writeValueAsString(response));
            function.apply(webhookRequest);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
