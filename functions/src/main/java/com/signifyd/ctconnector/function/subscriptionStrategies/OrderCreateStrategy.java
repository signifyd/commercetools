package com.signifyd.ctconnector.function.subscriptionStrategies;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.commercetools.api.models.common.Money;
import com.commercetools.api.models.common.MoneyBuilder;
import com.commercetools.api.models.message.Message;
import com.commercetools.api.models.message.OrderCreatedMessage;
import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.PaymentMethodInfo;
import com.commercetools.api.models.payment.PaymentMethodInfoBuilder;
import com.commercetools.api.models.payment.TransactionDraft;
import com.commercetools.api.models.payment.TransactionDraftBuilder;
import com.commercetools.api.models.type.FieldContainer;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.commercetools.enums.OrderChannel;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.PaymentMethod;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import com.commercetools.api.models.payment.TransactionState;
import com.commercetools.api.models.payment.TransactionType;

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

        Payment payment = this.commercetoolsClient
                .getPaymentById(OrderHelper.getMostRecentPaymentIdFromOrder(order));

        if (order.getCustom() == null) {
            order = setOrderCustomType(order);
        } else if (order.getCustom().getFields().values().get(CustomFields.CHECKOUT_ID) == null) {
            order = setOrderCheckoutId(order);
        }

        if (payment == null) {
            payment = createPayment(order);
            order = addPaymentToOrder(order, payment);
        }

        SaleApiWrapper apiWrapper = new SaleApiWrapper(commercetoolsClient, signifydClient, configReader, order,
                payment);
        apiWrapper.execute();
    }

    private Order setOrderCustomType(Order order) {
        FieldContainer fields = FieldContainer.builder()
                .addValue(CustomFields.CHECKOUT_ID, UUID.randomUUID().toString())
                .addValue(CustomFields.ORDER_CHANNEL, OrderChannel.PHONE.name())
                .build();
        return this.commercetoolsClient.setCustomType(order, CustomFields.SIGNIFYD_ORDER_TYPE_KEY, fields);
    }

    private Order setOrderCheckoutId(Order order) {
        order.getCustom().getFields().values().put(CustomFields.CHECKOUT_ID, UUID.randomUUID().toString());
        FieldContainer fields = order.getCustom().getFields();
        return this.commercetoolsClient.setCustomFields(order, fields);
    }

    private Payment createPayment(Order order) {
        Money amountPlanned = MoneyBuilder.of()
                .centAmount(order.getTotalPrice().getCentAmount())
                .currencyCode(order.getTotalPrice().getCurrencyCode())
                .build();

        PaymentMethodInfo paymentMethodInfo = PaymentMethodInfoBuilder.of()
                .method(PaymentMethod.GIFT_CARD.name())
                .build();

        List<TransactionDraft> transactions = new ArrayList<>();
        transactions.add(
                TransactionDraftBuilder.of()
                        .type(TransactionType.AUTHORIZATION)
                        .state(TransactionState.SUCCESS)
                        .amount(amountPlanned)
                        .build());

        return this.commercetoolsClient.createPayment(amountPlanned, paymentMethodInfo, transactions);
    }

    private Order addPaymentToOrder(Order order, Payment payment) {
        return this.commercetoolsClient.addPaymentToOrder(order, payment);
    }
}
