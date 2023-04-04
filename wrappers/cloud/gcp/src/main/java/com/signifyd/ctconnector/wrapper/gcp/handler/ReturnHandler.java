package com.signifyd.ctconnector.wrapper.gcp.handler;

import com.commercetools.api.models.order.OrderReference;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.signifyd.ctconnector.function.ReturnFunction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;

import io.vrap.rmf.base.client.http.HttpStatusCode;

import java.io.PrintWriter;
import java.util.stream.Collectors;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class ReturnHandler implements HttpFunction {
    private static final ReturnFunction function = new ReturnFunction();
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final ConfigReader configReader = new ConfigReader();
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {
        String requestBody = httpRequest.getReader().lines().collect(Collectors.joining());
        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode()) || requestBody == null) {
            httpResponse.setStatusCode(HttpStatusCode.OK_200);
            return;
        }

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            ExtensionRequest<OrderReference> request = objectMapper.readValue(requestBody, ExtensionRequest.class);
            ExtensionResponse<OrderUpdateAction> result = function.apply(request);
            if (result.isErrorResponse()) {
                httpResponse.setStatusCode(HttpStatusCode.BAD_REQUEST_400);
                logger.info("Return prevented returning with 400 code:" + objectMapper.writeValueAsString(result.getErrors()));
            }
            String rawBody = objectMapper.writeValueAsString(result);
            PrintWriter writer = new PrintWriter(httpResponse.getWriter());
            httpResponse.setContentType("application/json");
            writer.printf(rawBody);
            logger.debug("Sending response: {}", rawBody);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
