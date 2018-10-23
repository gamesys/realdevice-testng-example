package com.qa.utils;

import org.apache.log4j.Logger;

public class PropertiesHelper {

    public static java.util.Properties loadPropertiesFile(String propertiesFilePath) {
        java.util.Properties properties = new java.util.Properties();
        try {
            properties.load(Thread.currentThread().getClass().getResourceAsStream(propertiesFilePath));
        } catch (Exception e) {
            Reporter.error(e.getMessage());
        }
        return properties;
    }
}
