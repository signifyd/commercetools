package azure;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderReference;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;
import com.signifyd.ctconnector.function.PreAuthFunction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.config.ConfigReader;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class PreAuthHandler {
    private static final PreAuthFunction function = new PreAuthFunction();
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final ConfigReader configReader = new ConfigReader();

    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    @FunctionName("preauth")
    public HttpResponseMessage run(
            @HttpTrigger(
                    name = "req",
                    methods = {HttpMethod.POST},
                    authLevel = AuthorizationLevel.FUNCTION)
            HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) {
        var preAuthRequest = new ExtensionRequest<OrderReference>();
        String bodyRaw = request.getBody().orElseThrow();
        try {
            var data = objectMapper.readValue(bodyRaw, preAuthRequest.getClass());
            Order order = ((OrderReference) data.getResource()).getObj();
            if (!configReader.isPreAuth(order.getCountry())) {
                return request
                        .createResponseBuilder(HttpStatus.OK)
                        .build();
            }
            var result = function.apply(data);
            var responseBody = objectMapper.writeValueAsString(result);
            if (result.isErrorResponse()) {
                logger.info("Order creation prevented returning with 400 code");
                return request
                        .createResponseBuilder(HttpStatus.BAD_REQUEST)
                        .header("Content-Type", "application/json")
                        .body(responseBody)
                        .build();
            }
            logger.debug("Sending response: {}", responseBody);
            return request
                    .createResponseBuilder(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body(responseBody)
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
