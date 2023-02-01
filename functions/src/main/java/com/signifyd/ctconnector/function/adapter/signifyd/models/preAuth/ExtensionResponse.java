package com.signifyd.ctconnector.function.adapter.signifyd.models.preAuth;

import com.commercetools.api.models.ResourceUpdateAction;
import com.commercetools.api.models.common.LocalizedString;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtensionResponse<T extends ResourceUpdateAction<T>> {

    public ExtensionResponse() {
    }

    public ExtensionResponse(List<T> actions) {
        this.actions = actions;
    }

    @JsonProperty
    private List<T> actions;
    @JsonProperty
    private int statusCode;
    @JsonProperty
    private String message;
    @JsonProperty
    private LocalizedString localizedMessage;
    @JsonProperty
    private String responseType;
    @JsonProperty
    private List<ExtensionError> errors;

    public void addAction(T action) {
        if (actions == null) {
            actions = new ArrayList<>();
        }
        actions.add(action);
    }

    public void setActions(List<T> actions) {
        this.actions = actions;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }

    public void setLocalizedMessage(LocalizedString localizedMessage) {
        this.localizedMessage = localizedMessage;
    }

    public void setResponseType(String responseType) {
        this.responseType = responseType;
    }

    public void addError(ExtensionError error) {
        if (errors == null) {
            errors = new ArrayList<>();
        }
        errors.add(error);
    }

    @JsonIgnore
    public boolean isErrorResponse() {
        return errors != null && !errors.isEmpty();
    }
}

