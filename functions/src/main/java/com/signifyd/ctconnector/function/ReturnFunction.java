package com.signifyd.ctconnector.function;

import ch.qos.logback.classic.Logger;
import io.vrap.rmf.base.client.http.HttpStatusCode;

import com.commercetools.api.models.common.LocalizedString;
import com.commercetools.api.models.order.*;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.PropertyReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.returnFunctionStrategies.AttemptReturnFunction;
import com.signifyd.ctconnector.function.returnFunctionStrategies.ExecuteReturnFunction;
import com.signifyd.ctconnector.function.returnFunctionStrategies.ReturnFunctionStrategy;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionError;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.function.Function;

public class ReturnFunction
        implements Function<ExtensionRequest<OrderReference>, ExtensionResponse<OrderUpdateAction>> {

    protected final ConfigReader configReader;
    protected final SignifydClient signifydClient;
    protected final SignifydMapper signifydMapper;
    protected final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public ReturnFunction() {
        this.configReader = new ConfigReader();
        this.signifydClient = new SignifydClient(configReader);
        this.signifydMapper = new SignifydMapper();
    }

    public ReturnFunction(ConfigReader configReader,
            SignifydClient signifydClient,
            PropertyReader propertyReader,
            SignifydMapper signifydMapper) {
        this.configReader = configReader;
        this.signifydClient = signifydClient;
        this.signifydMapper = signifydMapper;
    }

    @Override
    public ExtensionResponse<OrderUpdateAction> apply(ExtensionRequest<OrderReference> request) {
        Order order = request.getResource().getObj();

        try {
            OrderHelper.controlOrderSentToSignifyd(order);
        } catch (RuntimeException e) {
            return generateErrorResponse(order.getCountry(), e.getMessage());
        }

        ReturnInfo returnInfo = OrderHelper.getMostRecentReturnInfoFromOrder(order);
        ReturnFunctionStrategy function;
        if (returnInfo.getItems().get(0).getCustom() != null
                && returnInfo.getItems().get(0).getCustom().getFields().values()
                        .get(CustomFields.RETURN_ITEM_RAW_ATTEMPT_RESPONSE) != null) {
            function = new ExecuteReturnFunction(configReader, signifydClient, signifydMapper);
        } else {
            function = new AttemptReturnFunction(configReader, signifydClient, signifydMapper);
        }

        try {
            return function.execute(order, returnInfo);
        } catch (Signifyd4xxException | Signifyd5xxException | IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            return generateErrorResponse(order.getCountry(), e.getMessage());
        }
    }

    private ExtensionResponse<OrderUpdateAction> generateErrorResponse(String country, String message) {
        logger.info(message);
        ExtensionResponse<OrderUpdateAction> response = new ExtensionResponse<>();
        LocalizedString localizedMessage = LocalizedString.builder().addValue(country, message).build();
        response.setStatusCode(HttpStatusCode.BAD_REQUEST_400);
        response.addError(
                ExtensionError.builder()
                        .code(CustomFields.INVALID_INPUT)
                        .message(message)
                        .localizedMessage(localizedMessage)
                        .build());
        return response;
    }
}
