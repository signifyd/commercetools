package com.signifyd.ctconnector.function.config;

import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyReader {

    private final Properties prop;
    private final Logger logger = (Logger) LoggerFactory.getLogger(getClass().getName());
    public PropertyReader() {
        this.prop = new Properties();
        try (InputStream input = this.getClass().getClassLoader().getResourceAsStream("pom.properties")) {
            prop.load(input);
        } catch (IOException ioex){
            logger.error("Problem occurred while reading the POM properties " + ioex.getMessage());
        }
    }

    public String getCommercetoolsSDKVersion(){
        return this.prop.getProperty("commercetools.version");
    }

    public String getSignifydClientVersion(){
        return this.prop.getProperty("signifyd-client.version");
    }
}
