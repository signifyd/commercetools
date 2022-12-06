package com.signifyd.ctconnector.function.config.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ConfigModel {
    @JsonProperty(value = "EXECUTION_MODE")
    private ExecutionMode executionMode;
    @JsonProperty(value = "DEFAULT_LOCALE",required = true)
    private String locale;
    @JsonProperty(value = "ROOT_LOGGER_LEVEL")
    private String loggerLevel;
    @JsonProperty(value = "CREDENTIALS",required = true)
    private Credentials credentials;
    @JsonProperty(value = "DEFAULT_CONFIGURATION", required = true)
    private Configuration configuration;
    @JsonProperty(value = "COUNTRY_CONFIGURATIONS")
    private Map<String, Configuration> countryConfigurations;
    @JsonProperty(value = "DATA_MAPPING", required = true)
    private DataMappingConfig dataMappingConfig;
    @JsonProperty(value = "EXCLUDED_PAYMENT_METHODS")
    private List<String> excludedPaymentMethods;

}
