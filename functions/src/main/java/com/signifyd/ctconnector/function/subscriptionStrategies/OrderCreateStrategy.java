package com.signifyd.ctconnector.function.subscriptionStrategies;

import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.message.OrderCreatedMessage;
import com.commercetools.api.models.payment.Payment;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.utils.OrderHelper;

public class OrderCreateStrategy implements SubscriptionStrategy {
    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final ConfigReader configReader;

    public OrderCreateStrategy(CommercetoolsClient commercetoolsClient, SignifydClient signifydClient, ConfigReader configReader) {
        this.commercetoolsClient = commercetoolsClient;
        this.signifydClient = signifydClient;
        this.configReader = configReader;
    }

    @Override
    public void execute(Message message) {
        OrderCreatedMessage createdMessage = (OrderCreatedMessage) message;
        Payment payment = this.commercetoolsClient.getPaymentById(OrderHelper.getMostRecentPaymentIdFromOrder(createdMessage.getOrder()));
        SaleApiWrapper apiWrapper = new SaleApiWrapper(commercetoolsClient, signifydClient, configReader, createdMessage.getOrder(), payment);
        apiWrapper.execute();
    }
}
