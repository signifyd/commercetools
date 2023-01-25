package com.signifyd.ctconnector.function.adapter.signifyd.enums;

import java.util.HashMap;
import java.util.Map;

public enum ReturnItemReason {
    BETTER_PRICE_AVAILABLE("BETTER_PRICE_AVAILABLE"),
    BOUGHT_BY_MISTAKE("BOUGHT_BY_MISTAKE"),
    CANCELATION("CANCELATION"),
    DID_NOT_AUTHORIZE("DID_NOT_AUTHORIZE"),
    DUPLICATE_ORDER("DUPLICATE_ORDER"),
    ITEM_ARRIVED_TOO_LATE("ITEM_ARRIVED_TOO_LATE"),
    ITEM_NOT_RECEIVED("ITEM_NOT_RECEIVED"),
    NO_LONGER_NEEDED("NO_LONGER_NEEDED"),
    ITEM_DAMAGED("ITEM_DAMAGED"),
    SIGNIFICANTLY_NOT_AS_DESCRIBED("SIGNIFICANTLY_NOT_AS_DESCRIBED"),
    ORDER_ERROR("ORDER_ERROR"),
    CANCELLED_BY_ISSUER("CANCELLED_BY_ISSUER"),
    OTHER("OTHER");

    private final String value;
    private static final Map<String, ReturnItemReason> lookup = new HashMap<String, ReturnItemReason>();

    static {
        for (ReturnItemReason r : ReturnItemReason.values()) {
            lookup.put(r.getValue().toString(), r);
        }
    }

    ReturnItemReason(final String newValue) {
        this.value = newValue;
    }

    public String getValue() { return value; }
}
