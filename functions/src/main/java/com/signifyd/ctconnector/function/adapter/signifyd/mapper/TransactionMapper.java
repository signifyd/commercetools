package com.signifyd.ctconnector.function.adapter.signifyd.mapper;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.Transaction;
import com.commercetools.api.models.payment.TransactionState;
import com.signifyd.ctconnector.function.adapter.signifyd.enums.GatewayStatusCode;
import com.signifyd.ctconnector.function.adapter.signifyd.models.BillingAddress;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.CheckoutPaymentDetails;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionModel;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.utils.Price;
import lombok.Getter;

import java.util.List;

@Getter
public abstract class TransactionMapper {
    private final Order order;
    private final Payment payment;
    private final ConfigReader configReader;

    public TransactionMapper(Order order, Payment payment, ConfigReader configReader) {
        this.order = order;
        this.payment = payment;
        this.configReader = configReader;
    }

    protected TransactionModel mapDefaultFields(Transaction transaction) {
        String paymentMethod = adjustPaymentMethodName(payment.getPaymentMethodInfo().getMethod());
        return TransactionModel.builder()
                .checkoutPaymentDetails(buildCheckoutPaymentDetails())
                .gateway(payment.getPaymentMethodInfo().getPaymentInterface())
                .currency(transaction.getAmount().getCurrencyCode())
                .paymentMethod(paymentMethod)
                .amount(Price.commerceToolsPrice(transaction.getAmount()))
                .build();

    }

    protected GatewayStatusCode adjustGatewayStatusCode(TransactionState state) {
        if (TransactionState.INITIAL.equals(state) || TransactionState.PENDING.equals(state)) {
            return GatewayStatusCode.valueOf(GatewayStatusCode.PENDING.name());
        } else if (TransactionState.SUCCESS.equals(state)) {
            return GatewayStatusCode.SUCCESS;
        } else if (TransactionState.FAILURE.equals(state)) {
            return GatewayStatusCode.FAILURE;
        } else {
            throw new IllegalArgumentException();
        }
    }

    private String adjustPaymentMethodName(String paymentMethod) {
        var paymentMap = configReader.getPaymentMethodMapping();
        if (!paymentMap.containsKey(paymentMethod)) {
            return paymentMethod;
        }
        return paymentMap.get(paymentMethod);
    }

    private CheckoutPaymentDetails buildCheckoutPaymentDetails() {
        var checkoutPaymentDetailsBuilder = CheckoutPaymentDetails
                .builder();

        if (order.getBillingAddress() != null) {
            checkoutPaymentDetailsBuilder.billingAddress(buildBillingAddress());
        }

        if (payment != null && payment.getCustom() != null) {
            var paymentCustomFields = payment.getCustom().getFields().values();
            if (paymentCustomFields.containsKey(CustomFields.CARD_HOLDER_NAME)) {
                checkoutPaymentDetailsBuilder.accountHolderName(paymentCustomFields.get(CustomFields.CARD_HOLDER_NAME).toString());
            }
            if (paymentCustomFields.containsKey(CustomFields.CARD_BIN)) {
                checkoutPaymentDetailsBuilder.cardBin(paymentCustomFields.get(CustomFields.CARD_BIN).toString());
            }
            if (paymentCustomFields.containsKey(CustomFields.CARD_LAST_FOUR)) {
                checkoutPaymentDetailsBuilder.cardLast4(paymentCustomFields.get(CustomFields.CARD_LAST_FOUR).toString());
            }
            if (paymentCustomFields.containsKey(CustomFields.CARD_EXPIRY_MONTH)) {
                checkoutPaymentDetailsBuilder.cardExpiryMonth(Integer
                        .parseInt(paymentCustomFields.get(CustomFields.CARD_EXPIRY_MONTH).toString()));
            }
            if (paymentCustomFields.containsKey(CustomFields.CARD_EXPIRY_YEAR)) {
                checkoutPaymentDetailsBuilder.cardExpiryYear(Integer
                        .parseInt(paymentCustomFields.get(CustomFields.CARD_EXPIRY_YEAR).toString()));
            }
        }
        return checkoutPaymentDetailsBuilder.build();
    }

    private BillingAddress buildBillingAddress() {
        return BillingAddress.builder()
                .streetAddress(order.getBillingAddress().getStreetName())
                .unit(order.getBillingAddress().getAdditionalStreetInfo())
                .postalCode(order.getBillingAddress().getPostalCode())
                .city(order.getBillingAddress().getCity())
                .provinceCode(order.getBillingAddress().getCountry())
                .countryCode(order.getBillingAddress().getCountry())
                .build();
    }

    public abstract List<TransactionModel> generateTransactions();
}
