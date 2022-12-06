package azure;

import azure.models.EventSchema;
import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.message.Message;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microsoft.azure.functions.annotation.*;
import com.microsoft.azure.functions.*;
import com.signifyd.ctconnector.function.SubscriptionFunction;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import org.slf4j.LoggerFactory;

public class SubscriptionEventGridHandler {

    private static final ConfigReader configReader = new ConfigReader();
    private static final SubscriptionFunction function = new SubscriptionFunction();
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @FunctionName("subscriptionEventGrid")
    public void logEvent(
            @EventGridTrigger(
                    name = "event"
            )
            EventSchema event,
            final ExecutionContext context) {
        if (ExecutionMode.DISABLED.equals(configReader.getExecutionMode())) {
            return;
        }

        Message message = objectMapper.convertValue(event.data, Message.class);
        function.apply(message);
    }
}
