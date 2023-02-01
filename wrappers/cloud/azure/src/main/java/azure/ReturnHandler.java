package azure;

import com.commercetools.api.models.order.OrderReference;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatusType;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.signifyd.ctconnector.function.ReturnFunction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;

import org.slf4j.LoggerFactory;
import ch.qos.logback.classic.Logger;
import io.vrap.rmf.base.client.http.HttpStatusCode;

import java.util.Optional;

public class ReturnHandler {
    private static final ReturnFunction function = new ReturnFunction();
    private static final ConfigReader configReader = new ConfigReader();
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @FunctionName("extensions")
    public HttpResponseMessage run(
            @HttpTrigger(name = "req", methods = {
                    HttpMethod.POST }, authLevel = AuthorizationLevel.FUNCTION) HttpRequestMessage<Optional<String>> httpRequestMessage,
            final ExecutionContext context
    ) {
        HttpResponseMessage.Builder responseBuilder = httpRequestMessage
                .createResponseBuilder(HttpStatusType.custom(HttpStatusCode.OK_200))
                .header("Content-Type", "application/json");

        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode()) || httpRequestMessage.getBody() == null) {
            return responseBuilder.build();
        }

        try {
            ExtensionRequest<OrderReference> request = objectMapper.readValue(httpRequestMessage.getBody().orElseThrow(), ExtensionRequest.class);
            ExtensionResponse<OrderUpdateAction> result = function.apply(request);
            if (result.isErrorResponse()) {
                responseBuilder.status(HttpStatusType.custom(HttpStatusCode.BAD_REQUEST_400));
                logger.info("Return prevented returning with 400 code:" + result.getMessage());
            }
            String rawBody = objectMapper.writeValueAsString(result);
            logger.debug("Sending response: {}", rawBody);
            return responseBuilder.body(rawBody).build();

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
