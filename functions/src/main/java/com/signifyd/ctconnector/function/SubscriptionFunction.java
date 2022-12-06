package com.signifyd.ctconnector.function;

import com.commercetools.api.models.message.*;
import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.subscriptionStrategies.*;
import org.apache.commons.lang3.NotImplementedException;

import java.util.function.Function;

public class SubscriptionFunction implements Function<Message, String> {

    private final ConfigReader configReader;
    private final CommercetoolsClient commercetoolsClient;
    private final SignifydClient signifydClient;

    public SubscriptionFunction() {
        this.configReader = new ConfigReader();
        this.commercetoolsClient = new CommercetoolsClient(this.configReader);
        this.signifydClient = new SignifydClient(this.configReader);
    }

    public SubscriptionFunction(
            ConfigReader configReader,
            CommercetoolsClient commercetoolsClient,
            SignifydClient signifydClient
    ) {
        this.configReader = configReader;
        this.commercetoolsClient = commercetoolsClient;
        this.signifydClient = signifydClient;
    }

    @Override
    public String apply(Message message) {
        SubscriptionStrategy subscriptionStrategy;

        switch (message.getType()) {
            case PaymentTransactionAddedMessage.PAYMENT_TRANSACTION_ADDED: {
                subscriptionStrategy = new TransactionApiStrategy(this.commercetoolsClient, this.signifydClient, this.configReader);
                break;
            }
            case PaymentTransactionStateChangedMessage.PAYMENT_TRANSACTION_STATE_CHANGED: {
                subscriptionStrategy = new PaymentTransactionStateChangedStrategy(this.commercetoolsClient, this.signifydClient, this.configReader);
                break;
            }
            case OrderPaymentStateChangedMessage.ORDER_PAYMENT_STATE_CHANGED: {
                subscriptionStrategy = new OrderPaymentStateChangedStrategy(this.commercetoolsClient, this.signifydClient, this.configReader);
                break;
            }
            case OrderCreatedMessage.ORDER_CREATED: {
                subscriptionStrategy = new OrderCreateStrategy(this.commercetoolsClient, this.signifydClient, this.configReader);
                break;
            }
            case OrderShippingAddressSetMessage.ORDER_SHIPPING_ADDRESS_SET: {
                subscriptionStrategy = new RerouteApiStrategy(this.commercetoolsClient, this.signifydClient);
                break;
            }
            case DeliveryAddedMessage.DELIVERY_ADDED: {
                subscriptionStrategy = new FulFillmentApiStrategy(this.commercetoolsClient, this.signifydClient, this.configReader);
                break;
            }
            case OrderLineItemAddedMessage.ORDER_LINE_ITEM_ADDED:
            case OrderLineItemRemovedMessage.ORDER_LINE_ITEM_REMOVED:
            case OrderLineItemDiscountSetMessage.ORDER_LINE_ITEM_DISCOUNT_SET:
            case OrderCustomLineItemAddedMessage.ORDER_CUSTOM_LINE_ITEM_ADDED:
            case OrderCustomLineItemRemovedMessage.ORDER_CUSTOM_LINE_ITEM_REMOVED:
            case OrderCustomLineItemQuantityChangedMessage.ORDER_CUSTOM_LINE_ITEM_QUANTITY_CHANGED:
            case OrderCustomLineItemDiscountSetMessage.ORDER_CUSTOM_LINE_ITEM_DISCOUNT_SET:
            case OrderShippingInfoSetMessage.ORDER_SHIPPING_INFO_SET:
            case OrderShippingRateInputSetMessage.ORDER_SHIPPING_RATE_INPUT_SET:
            case OrderDiscountCodeAddedMessage.ORDER_DISCOUNT_CODE_ADDED:
            case OrderDiscountCodeRemovedMessage.ORDER_DISCOUNT_CODE_REMOVED:
            case OrderDiscountCodeStateSetMessage.ORDER_DISCOUNT_CODE_STATE_SET:
            case OrderCustomerGroupSetMessage.ORDER_CUSTOMER_GROUP_SET: {
                subscriptionStrategy = new RepriceApiStrategy(
                        this.commercetoolsClient,
                        this.signifydClient,
                        this.configReader
                );
                break;
            }
            default:
                throw new NotImplementedException(String.format("Received message type (%s) is not supported", message.getType()));
        }
        subscriptionStrategy.execute(message);
        return null;
    }
}
