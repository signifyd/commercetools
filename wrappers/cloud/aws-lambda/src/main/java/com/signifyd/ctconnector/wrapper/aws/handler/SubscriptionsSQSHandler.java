package com.signifyd.ctconnector.wrapper.aws.handler;


import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import com.commercetools.api.models.message.Message;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.signifyd.ctconnector.function.SubscriptionFunction;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;

public class SubscriptionsSQSHandler implements RequestHandler<SQSEvent, Void> {

    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ConfigReader configReader = new ConfigReader();
    private static final SubscriptionFunction function = new SubscriptionFunction();

    @Override
    public Void handleRequest(SQSEvent event, Context context)
    {
        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode())) {
            return null;
        }
        LambdaLogger logger = context.getLogger();

        logger.log("SQSEvent :");
        logger.log(event.toString());

        for(SQSEvent.SQSMessage msg : event.getRecords()){
            logger.log("SQS Message Body : " + msg.getBody());
            try {
                var data = objectMapper.readValue(msg.getBody(), Message.class);
                var result = function.apply(data);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return null;
    }
}