package com.signifyd.ctconnector.wrapper.aws.handler;

import ch.qos.logback.classic.Logger;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signifyd.ctconnector.function.PreAuthFunction;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.wrapper.aws.response.PreauthResponse;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class PreAuthHandler implements RequestHandler<Map<String, Object>, PreauthResponse> {

    private static final PreAuthFunction function = new PreAuthFunction();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ConfigReader configReader = new ConfigReader();

    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    @Override
    public PreauthResponse handleRequest(Map<String, Object> event, Context context) {

        PreauthResponse response = new PreauthResponse();
        response.setStatusCode(200);
        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode())) {
            return response;
        }
        if (event.get("body") == null) {
            return response;
        }

        var request = new ExtensionRequest<OrderReference>();
        try {
            var eventObjectRaw = objectMapper.writeValueAsString(event);
            logger.debug("Event Object: {}", eventObjectRaw);
            var data = objectMapper.readValue(event.get("body").toString(), request.getClass());
            Order order = ((OrderReference) data.getResource()).getObj();
            if (!configReader.isPreAuth(order.getCountry())) {
                response.setStatusCode(200);
                return response;
            }
            var result = function.apply(data);
            var rawBody = objectMapper.writeValueAsString(result);
            if (result.isErrorResponse()) {
                response.setStatusCode(400);
                logger.info("Order creation prevented returning with 400 code");
            }
            logger.debug("Sending response: {}", rawBody);
            response.setBody(rawBody);
            return response;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}