package com.signifyd.ctconnector.function.proxyStrategies;

import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyRequest;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResource;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResponse;

public interface ProxyStrategy {
    public ProxyResponse execute(ProxyRequest<ProxyResource> request);
}
