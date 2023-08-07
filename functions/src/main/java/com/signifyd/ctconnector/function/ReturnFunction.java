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
import com.signifyd.ctconnector.function.constants.CustomFields;
import com.signifyd.ctconnector.function.returnFunctionStrategies.AttemptReturnStrategy;
import com.signifyd.ctconnector.function.returnFunctionStrategies.ExecuteReturnStrategy;
import com.signifyd.ctconnector.function.returnFunctionStrategies.ReturnStrategy;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionError;
import com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth.ExtensionRequest;
import com.signifyd.ctconnector.function.utils.OrderHelper;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;

public class ReturnFunction
        implements Function<ExtensionRequest<OrderReference>, ExtensionResponse<OrderUpdateAction>> {

    protected final SignifydClient signifydClient;
    protected final SignifydMapper signifydMapper;
    protected final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());

    public ReturnFunction() {
        this.signifydClient = new SignifydClient(new ConfigReader());
        this.signifydMapper = new SignifydMapper();
    }

    public ReturnFunction(
        SignifydClient signifydClient,
        SignifydMapper signifydMapper
    ) {
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
        ReturnInfoTransition transition = null;

        for (ReturnInfo rInfo : order.getReturnInfo()) {
            if (rInfo.getItems().stream().allMatch(
                    rItem -> rItem.getShipmentState() == ReturnShipmentState.RETURNED
                            && rItem.getCustom() == null)) {
                transition = ReturnInfoTransition.ATTEMPT;
                returnInfo = rInfo;
                break;
            } else if (rInfo.getItems().stream().anyMatch(
                    rItem -> rItem.getShipmentState().equals(ReturnShipmentState.BACK_IN_STOCK)
                            && rItem.getCustom() != null
                            && rItem.getCustom().getFields().values()
                                    .getOrDefault(CustomFields.RETURN_ITEM_TRANSITION, false)
                                    .toString().equals(ReturnInfoTransition.ATTEMPT.name()))) {
                transition = ReturnInfoTransition.EXECUTE;
                returnInfo = rInfo;
                break;
            }
        }

        if (transition == null) {
            return new ExtensionResponse<OrderUpdateAction>(new ArrayList<>());
        }

        ReturnStrategy function = null;
        if (transition.equals(ReturnInfoTransition.ATTEMPT)) {
            logger.info("Return is ATTEMPTING");
            function = new AttemptReturnStrategy(signifydClient, signifydMapper);
        } else if (transition.equals(ReturnInfoTransition.EXECUTE)) {
            logger.info("Return is EXECUTING");
            function = new ExecuteReturnStrategy(signifydClient);
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
