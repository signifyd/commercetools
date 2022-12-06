package com.signifyd.ctconnector.function.subscriptionStrategies;

import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.message.OrderPaymentStateChangedMessage;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.order.PaymentState;
import com.commercetools.api.models.payment.Payment;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.utils.OrderHelper;

public class OrderPaymentStateChangedStrategy implements SubscriptionStrategy {
    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final ConfigReader configReader;

    public OrderPaymentStateChangedStrategy(
            CommercetoolsClient commercetoolsClient,
            SignifydClient signifydClient,
            ConfigReader configReader
    ) {
        this.commercetoolsClient = commercetoolsClient;
        this.signifydClient = signifydClient;
        this.configReader = configReader;


    }

    @Override
    public void execute(Message message) {
        OrderPaymentStateChangedMessage parsedMessage = (OrderPaymentStateChangedMessage) message;
        if (!parsedMessage.getPaymentState().equals(PaymentState.PAID)) {
            return;
        }
        Order order = commercetoolsClient.getOrderById(parsedMessage.getResource().getId());
        Payment payment = this.commercetoolsClient.getPaymentById(OrderHelper.getMostRecentPaymentIdFromOrder(order));
        SaleApiWrapper apiWrapper = new SaleApiWrapper(commercetoolsClient, signifydClient, configReader, order, payment);
        apiWrapper.execute();
    }
}
