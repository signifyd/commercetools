package com.signifyd.ctconnector.function.subscriptionStrategies;

import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.message.PaymentTransactionStateChangedMessage;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Payment;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.utils.OrderHelper;

public class PaymentTransactionStateChangedStrategy implements SubscriptionStrategy {

    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final ConfigReader configReader;

    public PaymentTransactionStateChangedStrategy(
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
        PaymentTransactionStateChangedMessage stateChangedMessage = (PaymentTransactionStateChangedMessage) message;
        Order order = commercetoolsClient.getOrderByPaymentId(stateChangedMessage.getResource().getId());
        SubscriptionStrategy subscriptionStrategy;
        if (order.getCustom().getFields().values().containsKey(CustomFields.IS_SENT_TO_SIGNIFYD)) {
            subscriptionStrategy = new TransactionApiStrategy(commercetoolsClient, signifydClient, configReader);
            subscriptionStrategy.execute(message);
        } else {
            Payment payment = this.commercetoolsClient.getPaymentById(OrderHelper.getMostRecentPaymentIdFromOrder(order));
            SaleApiWrapper apiWrapper = new SaleApiWrapper(commercetoolsClient, signifydClient, configReader, order, payment);
            apiWrapper.execute();
        }
    }
}
