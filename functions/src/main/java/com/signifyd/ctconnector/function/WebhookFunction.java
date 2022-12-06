package com.signifyd.ctconnector.function;

import ch.qos.logback.classic.Logger;
import com.commercetools.api.models.order.Order;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;
import com.signifyd.ctconnector.function.adapter.signifyd.models.webhook.WebhookRequest;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.decisionCommands.AcceptCommand;
import com.signifyd.ctconnector.function.decisionCommands.DecisionCommand;
import com.signifyd.ctconnector.function.decisionCommands.HoldCommand;
import com.signifyd.ctconnector.function.decisionCommands.RejectCommand;
import com.signifyd.ctconnector.function.utils.SignifydWebhookValidator;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class WebhookFunction implements Function<WebhookRequest, String> {

    private final ConfigReader configReader;
    private final CommercetoolsClient commercetoolsClient;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    public WebhookFunction() {
        this.configReader = new ConfigReader();
        this.commercetoolsClient = new CommercetoolsClient(configReader);
    }

    public WebhookFunction(ConfigReader configReader, CommercetoolsClient commercetoolsClient) {
        this.configReader = configReader;
        this.commercetoolsClient = commercetoolsClient;
    }

    @Override
    public String apply(WebhookRequest webhookRequest) {
        Action checkpointAction = webhookRequest.getDecisionResponse().getDecision().getCheckpointAction();

        if (SignifydWebhookValidator.isExcessiveWebHook(webhookRequest)) {
            logger.debug(checkpointAction.name() + " webhook action is not executed, webhook is excessive");
            return "";
        }

        Order order = commercetoolsClient.getOrderById(webhookRequest.getDecisionResponse().getOrderId());
        if(checkpointAction.equals(order.getCustom().getFields().values().get(CustomFields.FRAUD_CHECK_POINT_ACTION))){
            logger.debug(checkpointAction.name() + " received same decision action further process halted.");
            return "";
        }
        DecisionCommand command = null;
        switch (checkpointAction) {
            case ACCEPT:
                command = new AcceptCommand(configReader, commercetoolsClient, order, webhookRequest.getDecisionResponse());
                break;
            case REJECT:
                command = new RejectCommand(configReader, commercetoolsClient, order, webhookRequest.getDecisionResponse());
                break;
            case HOLD:
                command = new HoldCommand(configReader, commercetoolsClient, order, webhookRequest.getDecisionResponse());
                break;
            default:
                break;
        }
        if (command != null) {
            if (ExecutionMode.ACTIVE.equals(configReader.getExecutionMode())) {
                command.executeOrderActions();
            } else {
                command.executeStandardOrderActions();
            }
        }
        return null;
    }


}
