package com.signifyd.ctconnector.function.subscriptionStrategies;

import com.commercetools.api.models.message.Message;

public interface SubscriptionStrategy {
    public void execute(Message message);
}
