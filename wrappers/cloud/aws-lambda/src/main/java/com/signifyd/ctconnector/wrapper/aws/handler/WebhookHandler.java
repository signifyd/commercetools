package com.signifyd.ctconnector.wrapper.aws.handler;

import ch.qos.logback.classic.Logger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signifyd.ctconnector.function.WebhookFunction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.webhook.WebhookRequest;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.constants.SignifydApi;
import com.signifyd.ctconnector.function.utils.SignifydWebhookValidator;
import com.signifyd.ctconnector.wrapper.aws.response.WebhookResponse;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


public class WebhookHandler implements RequestHandler<Map<String, Object>, WebhookResponse> {

    private static final WebhookFunction function = new WebhookFunction();
    private static final ConfigReader configReader = new ConfigReader();
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Override
    public WebhookResponse handleRequest(Map<String, Object> event, Context context) {
        var rawBody = event.get("body").toString();
        var rawHeader = event.get("headers").toString();
        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode())
                || rawBody == null || rawHeader == null) {
            return new WebhookResponse(200, "");
        }
        String signifydSecHmacSha256 = this.parseHeader(rawHeader).get(SignifydApi.SHA_256_HEADER) + "=";

        if (!SignifydWebhookValidator.validateWebhook(signifydSecHmacSha256, rawBody, configReader.getSignifydTeamAPIKey())) {
            throw new IllegalArgumentException("Webhook is not valid.");
        }
        try {
            var response = objectMapper.readValue(rawBody, DecisionResponse.class);
            String signifydCheckpoint = this.parseHeader(rawHeader).get(SignifydApi.CHECK_POINT_HEADER);
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
        return new WebhookResponse(200, "");
    }

    private Map<String, String> parseHeader(String rawHeader) {
        rawHeader = rawHeader.substring(1, rawHeader.length() - 1);
        String[] keyValuePairs = rawHeader.split(", ");
        Map<String, String> map = new HashMap<>();

        for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            map.put(entry[0].trim(), entry[1].trim());
        }
        return map;
    }
}