package com.signifyd.ctconnector.function.adapter.signifyd.mapper;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Payment;
import com.signifyd.ctconnector.function.config.ConfigReader;

public class TransactionMapperFactory {
    private final Order order;
    private final Payment payment;
    private final ConfigReader configReader;

    public TransactionMapperFactory(Order order, Payment payment, ConfigReader configReader) {
        this.order = order;
        this.payment = payment;
        this.configReader = configReader;
    }

    public TransactionMapper generateTransactionMapper(TransactionMapperType transactionMapperType) {
        switch (transactionMapperType) {
            case PRE_AUTH:
                return new PreAuthTransactionMapper(this.order, this.payment, this.configReader);
            case POST_AUTH:
                return new PostAuthTransactionMapper(this.order, this.payment, this.configReader);
            default:
                throw new IllegalArgumentException();
        }
    }
}
