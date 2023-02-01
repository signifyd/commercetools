package com.signifyd.ctconnector.function.subscriptionStrategies;

import java.util.UUID;

import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.message.OrderCreatedMessage;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.type.FieldContainer;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.commercetools.enums.OrderChannel;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.utils.OrderHelper;

public class OrderCreateStrategy implements SubscriptionStrategy {
    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;
    private final ConfigReader configReader;

    public OrderCreateStrategy(CommercetoolsClient commercetoolsClient, SignifydClient signifydClient,
            ConfigReader configReader) {
        this.commercetoolsClient = commercetoolsClient;
        this.signifydClient = signifydClient;
        this.configReader = configReader;
    }

    @Override
    public void execute(Message message) {
        OrderCreatedMessage createdMessage = (OrderCreatedMessage) message;
        Order order = createdMessage.getOrder();

        if (configReader.isPreAuth(order.getCountry())) {
            return;
        }

        order = setOrderCustom(order);
        Payment payment = this.commercetoolsClient.getPaymentById(OrderHelper.getMostRecentPaymentIdFromOrder(order));
        SaleApiWrapper apiWrapper = new SaleApiWrapper(commercetoolsClient, signifydClient, configReader, order, payment);
        apiWrapper.execute();
    }

    private Order setOrderCustom(Order order) {
        if (order.getCustom() == null) {
            FieldContainer fields = FieldContainer.builder()
                .addValue(CustomFields.CHECKOUT_ID, UUID.randomUUID().toString())
                .addValue(CustomFields.ORDER_CHANNEL, OrderChannel.PHONE.name())
                .build();
            return this.commercetoolsClient.setCustomType(order, CustomFields.SIGNIFYD_ORDER_TYPE_KEY, fields);
        } else if (order.getCustom().getFields().values().get(CustomFields.CHECKOUT_ID) == null) {
            order.getCustom().getFields().values().put(CustomFields.CHECKOUT_ID, UUID.randomUUID().toString());
            FieldContainer fields = order.getCustom().getFields();
            return this.commercetoolsClient.setCustomFields(order, fields);
        }
        return order;
    }
}
