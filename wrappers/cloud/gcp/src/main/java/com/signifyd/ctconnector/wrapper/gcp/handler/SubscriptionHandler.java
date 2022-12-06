package com.signifyd.ctconnector.wrapper.gcp.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.events.cloud.pubsub.v1.Message;
import com.signifyd.ctconnector.function.SubscriptionFunction;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Logger;

public class SubscriptionHandler implements BackgroundFunction<Message> {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ConfigReader configReader = new ConfigReader();
    private static final SubscriptionFunction function = new SubscriptionFunction();
    private static final Logger logger = Logger.getLogger(SubscriptionHandler.class.getName());

    @Override
    public void accept(Message message, Context context) {
        if (message.getData() == null) {
            logger.info("No message provided");
            return;
        }

        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode())) {
            return;
        }

        String messageString = new String(
                Base64.getDecoder().decode(message.getData().getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);

        try {
            var data = objectMapper.readValue(messageString, com.commercetools.api.models.message.Message.class);
            var result = function.apply(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}