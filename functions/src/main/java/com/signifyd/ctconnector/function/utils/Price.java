package com.signifyd.ctconnector.function.utils;

import com.commercetools.api.models.common.TypedMoney;

public class Price {
    
    public static double commerceToolsPrice(TypedMoney price) {
        int centAmount = Math.toIntExact(price.getCentAmount());
        int fractionDigits = price.getFractionDigits();
        return centAmount / Math.pow(10, fractionDigits);
    }
}
