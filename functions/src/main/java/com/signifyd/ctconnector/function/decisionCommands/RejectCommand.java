package com.signifyd.ctconnector.function.decisionCommands;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.OrderUpdateAction;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.models.DecisionResponse;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.checkoutResponse.Action;
import com.signifyd.ctconnector.function.config.ConfigReader;

import java.util.List;

public class RejectCommand extends DecisionCommand {
    public RejectCommand(ConfigReader configReader,
                         CommercetoolsClient commercetoolsClient,
                         Order order,
                         DecisionResponse decisionResponse) {
        super(configReader, commercetoolsClient, order, decisionResponse, Action.REJECT);
    }

    @Override
    public List<OrderUpdateAction> generateOrderActions() {
        return super.generateDefaultOrderActions(configReader.getDecisionActions(order.getCountry()).getReject());
    }

    @Override
    public Order executeOrderActions() {
        return super.executeDefaultOrderActions(configReader.getDecisionActions(order.getCountry()).getReject());
    }
}
