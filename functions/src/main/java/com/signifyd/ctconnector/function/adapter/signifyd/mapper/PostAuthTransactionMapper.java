package com.signifyd.ctconnector.function.adapter.signifyd.mapper;

import com.commercetools.api.models.order.Order;
import com.commercetools.api.models.payment.Payment;
import com.commercetools.api.models.payment.Transaction;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.transaction.TransactionModel;
import com.signifyd.ctconnector.function.config.ConfigReader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PostAuthTransactionMapper extends TransactionMapper {

    public PostAuthTransactionMapper(Order order, Payment payment, ConfigReader configReader) {
        super(order, payment, configReader);
    }

    @Override
    public List<TransactionModel> generateTransactions() {
        List<Transaction> transactionList = null;
        List<TransactionModel> mappedTransactions = new ArrayList<>();
        if (this.getPayment() != null && this.getPayment().getTransactions() != null) {
            transactionList = this.getPayment().getTransactions().stream()
                    .filter(pt -> pt.getType()
                            .equals(com.commercetools.api.models.payment.TransactionType.AUTHORIZATION)).collect(Collectors.toList());
        }
        if (transactionList == null) {
            return Collections.emptyList();
        }
        for (Transaction transaction : transactionList) {
            TransactionModel mappedTransaction = mapDefaultFields(transaction);
            mappedTransaction.setTransactionId(transaction.getId());
            mappedTransaction.setGatewayStatusCode(adjustGatewayStatusCode(transaction.getState()));
            mappedTransactions.add(mappedTransaction);
        }
        return mappedTransactions;
    }
}
