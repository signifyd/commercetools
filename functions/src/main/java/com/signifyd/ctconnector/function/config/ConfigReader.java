package com.signifyd.ctconnector.function.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.signifyd.ctconnector.function.config.model.ConfigModel;
import com.signifyd.ctconnector.function.config.model.Configuration;
import com.signifyd.ctconnector.function.config.model.DecisionActionConfigs;
import com.signifyd.ctconnector.function.config.model.ExecutionMode;
import com.signifyd.ctconnector.function.config.model.phoneNumber.PhoneNumberField;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class ConfigReader {

    private final ConfigModel configModel;

    public ConfigReader() {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("config.yaml");
        var mapper = new ObjectMapper(new YAMLFactory());
        try {
            this.configModel = mapper.readValue(inputStream, ConfigModel.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getCommercetoolsClientId() {
        return this.configModel.getCredentials().getCommercetools().getClientId();
    }

    public String getCommercetoolsClientSecret() {
        return this.configModel.getCredentials().getCommercetools().getClientSecret();
    }

    public String getCommerceToolsProjectKey() {
        return this.configModel.getCredentials().getCommercetools().getProjectKey();
    }

    public String getCommercetoolsRegion() {

        return this.configModel.getCredentials().getCommercetools().getRegion();
    }

    public String getSignifydTeamAPIKey() {
        return this.configModel.getCredentials().getSignifyd().getTeamAPIKey();
    }

    public Configuration getConfiguration(String country) {
        if (this.configModel.getCountryConfigurations() != null
                && this.configModel.getCountryConfigurations().containsKey(country)) {
            return this.configModel.getCountryConfigurations().get(country);
        } else {
            return this.configModel.getConfiguration();
        }
    }

    public boolean isPreAuth(String country) {
        return getConfiguration(country).isPreAuth();
    }

    public boolean isScaEvaluationRequired(String country) {
        return getConfiguration(country).isScaEvaluationRequired();
    }

    public boolean isRecommendationOnly(String country) {
        return getConfiguration(country).isRecommendationOnly();
    }

    public String getLocale() {
        return this.configModel.getLocale();
    }

    public ExecutionMode getExecutionMode() {
        return this.configModel.getExecutionMode();
    }

    public DecisionActionConfigs getDecisionActions(String country) {
        return getConfiguration(country).getDecisionActions();
    }

    public Map<String, String> getPaymentMethodMapping() {
        return this.configModel.getDataMappingConfig().getPaymentMethods();
    }

    public PhoneNumberField getPhoneNumberFieldMapping() {
        return this.configModel.getDataMappingConfig().getPhoneNumberField();
    }
    
    public List<String> getExcludedPaymentMethods() {
        if (this.configModel.getExcludedPaymentMethods() != null) {
            return this.configModel.getExcludedPaymentMethods();
        }
        return new ArrayList<>();
    }
}
