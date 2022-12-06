package com.signifyd.ctconnector.function.adapter.signifyd.enums;

import com.commercetools.api.models.payment.TransactionState;

import java.util.HashMap;
import java.util.Map;

public enum GatewayStatusCode {

    SUCCESS(TransactionState.SUCCESS.name()),
    PENDING(TransactionState.PENDING.name()),
    FAILURE(TransactionState.FAILURE.name()),
    INITIAL(TransactionState.INITIAL.name()),
    ERROR("ERROR"),
    CANCELLED("CANCELLED"),
    EXPIRED("EXPIRED"),
    SOFTDECLINE("SOFTDECLINE");


    private final Object value;
    private static final Map<String, GatewayStatusCode> lookup = new HashMap<String, GatewayStatusCode>();

    static {
        for (GatewayStatusCode pm : GatewayStatusCode.values()) {
            lookup.put(pm.getValue().toString(), pm);
        }
    }

    GatewayStatusCode(String value) {
        this.value = value;
    }

    GatewayStatusCode(TransactionState value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }

}
