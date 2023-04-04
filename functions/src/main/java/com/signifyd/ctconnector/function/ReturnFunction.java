package com.signifyd.ctconnector.function;

import ch.qos.logback.classic.Logger;

import com.commercetools.api.models.common.LocalizedString;
import com.commercetools.api.models.error.InvalidOperationError;
import com.commercetools.api.models.order.*;
import com.signifyd.ctconnector.function.adapter.commercetools.enums.ReturnInfoTransition;
import com.signifyd.ctconnector.function.adapter.signifyd.SignifydClient;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd4xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.exception.Signifyd5xxException;
import com.signifyd.ctconnector.function.adapter.signifyd.mapper.SignifydMapper;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionResponse;
import com.signifyd.ctconnector.function.config.ConfigReader;
import com.signifyd.ctconnector.function.config.PropertyReader;
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.returnFunctionStrategies.AttemptReturnStrategy;
import com.signifyd.ctconnector.function.returnFunctionStrategies.ExecuteReturnStrategy;
import com.signifyd.ctconnector.function.returnFunctionStrategies.ReturnStrategy;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionError;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.NoSuchElementException;
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

        ReturnInfo returnInfo = null;
        ReturnInfoTransition transition = ReturnInfoTransition.ATTEMPT;
        for (ReturnInfo rInfo : order.getReturnInfo()) {
            if (rInfo.getItems().stream().anyMatch(
                    rItem -> rItem.getCustom() == null)) {
                returnInfo = rInfo;
                transition = ReturnInfoTransition.ATTEMPT;
                break;
            } else if (rInfo.getItems().stream().anyMatch(
                    rItem -> rItem.getShipmentState().equals(ReturnShipmentState.BACK_IN_STOCK)
                            && rItem.getCustom().getFields().values().get(CustomFields.RETURN_ITEM_TRANSITION)
                                    .toString().equals(ReturnInfoTransition.EXECUTE.name()))) {
                returnInfo = rInfo;
                transition = ReturnInfoTransition.EXECUTE;
                break;
            }
        }

        ReturnStrategy function;
        if (transition.equals(ReturnInfoTransition.ATTEMPT)) {
            function = new AttemptReturnStrategy(configReader, signifydClient, signifydMapper);
        } else if (transition.equals(ReturnInfoTransition.EXECUTE)) {
            function = new ExecuteReturnStrategy(configReader, signifydClient, signifydMapper);
        } else {
            return generateErrorResponse(order.getCountry(),
                    String.format("Received transition type (%s) is not supported", transition.name()));
        }

        try {
            return function.execute(order, returnInfo);
        } catch (Signifyd4xxException | Signifyd5xxException | IOException e) {
            throw new RuntimeException(e);
        } catch (NullPointerException e) {
            return generateErrorResponse(order.getCountry(), e.getMessage());
        } catch (NoSuchElementException e) {
            return generateErrorResponse(order.getCountry(),
                    String.format("No such return info with (%s)", transition.name()));
        }
    }

    private ExtensionResponse<OrderUpdateAction> generateErrorResponse(String country, String message) {
        logger.info(String.format("Return Function Error: %s", message));
        ExtensionResponse<OrderUpdateAction> response = new ExtensionResponse<>();
        LocalizedString localizedMessage = LocalizedString.builder().addValue(country, message).build();
        response.addError(
                ExtensionError.builder()
                        .code(InvalidOperationError.INVALID_OPERATION)
                        .message(message)
                        .localizedMessage(localizedMessage)
                        .build());
        return response;
    }
}
