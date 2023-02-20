package com.signifyd.ctconnector.function;

import com.signifyd.ctconnector.function.adapter.commercetools.CommercetoolsClient;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyRequest;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResource;
import com.signifyd.ctconnector.function.adapter.commercetools.models.proxy.ProxyResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.proxyStrategies.*;

import io.vrap.rmf.base.client.http.HttpStatusCode;
import java.util.function.Function;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

public class ProxyFunction implements Function<ProxyRequest<ProxyResource>, ProxyResponse> {

    private final CommercetoolsClient commercetoolsClient;
    private final String ADD_RETURN_INFO = "add-return-info";
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public ProxyFunction() {
        ConfigReader configReader = new ConfigReader();
        this.commercetoolsClient = new CommercetoolsClient(configReader);
    }

    public ProxyFunction(
        CommercetoolsClient commercetoolsClient
    ) {
        this.commercetoolsClient = commercetoolsClient;
    }

    @Override
    public ProxyResponse apply(ProxyRequest<ProxyResource> request) {
        ProxyStrategy proxyStrategy;
        switch (request.getAction()) {
            case ADD_RETURN_INFO: {
                proxyStrategy = new AddReturnInfoStrategy(this.commercetoolsClient);
                break;
            }
            default: {
                logger.error(String.format("Received request action (%s) is not supported", request.getAction()));
                return ProxyResponse.builder()
                    .statusCode(HttpStatusCode.BAD_REQUEST_400)
                    .succeed(false)
                    .message(String.format("Received request action (%s) is not supported", request.getAction()))
                    .build();
            }
        }
        return proxyStrategy.execute(request);
    }
}
