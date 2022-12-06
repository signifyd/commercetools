package com.signifyd.ctconnector.wrapper.gcp.handler;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import com.signifyd.ctconnector.function.PreAuthFunction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;

import java.io.PrintWriter;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class PreAuthHandler implements HttpFunction {
    private static final Logger logger = Logger.getLogger(PreAuthHandler.class.getName());
    private static final PreAuthFunction function = new PreAuthFunction();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private static final ConfigReader configReader = new ConfigReader();

    @Override
    public void service(HttpRequest httpRequest, HttpResponse httpResponse) throws Exception {

        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode())) {
            httpResponse.setStatusCode(200);
            return;
        }
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        var request = new ExtensionRequest<OrderReference>();
        try {
            var rawBody = httpRequest.getReader().lines().collect(Collectors.joining());
            logger.info(rawBody);
            var data = objectMapper.readValue(rawBody, request.getClass());
            Order order = ((OrderReference) data.getResource()).getObj();
            if (!configReader.isPreAuth(order.getCountry())) {
                httpResponse.setStatusCode(200);
                return;
            }
            var result = function.apply(data);
            var body = objectMapper.writeValueAsString(result);
            System.out.printf("Sending response: {}", body);
            if (result.isErrorResponse()){
                httpResponse.setStatusCode(400);
            }
            var writer = new PrintWriter(httpResponse.getWriter());
            httpResponse.setContentType("application/json");
            writer.printf(body);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
