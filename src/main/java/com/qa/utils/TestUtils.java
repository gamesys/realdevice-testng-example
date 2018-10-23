package com.qa.utils;

import io.appium.java_client.AppiumDriver;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

public class TestUtils {

    public static DesiredCapabilities createDesiredCaps(Properties defaultProperties, HashMap<String, Object> deviceConfigMap) {
        //setup desired caps for native app chrome beta.
        DesiredCapabilities capabilities = new DesiredCapabilities();

        for (String key : defaultProperties.stringPropertyNames()) {
            capabilities.setCapability(key, defaultProperties.getProperty(key));
        }

        for (String key: deviceConfigMap.keySet()){
            if (deviceConfigMap.get(key) != null && !deviceConfigMap.get(key).toString().isEmpty() && !deviceConfigMap.get(key).toString().equalsIgnoreCase("name")) {
                capabilities.setCapability(key, deviceConfigMap.get(key));
            }
        }
        return capabilities;
    }

    public static void pause(long milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch(InterruptedException e) {

        }
    }

    public static String takeScreenshot(AppiumDriver appiumDriver) {

        org.openqa.selenium.WebDriver augmentedDriver = new Augmenter().augment(appiumDriver);
        File screenshot = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy_HH-mm-ss.SS");
        // Generate the name of the file
        String currentDate = dateFormat.format(new Date()).toString();
        String deviceName = appiumDriver.getCapabilities().getCapability("testobject_device").toString();

        String filename = "target" + File.separator + currentDate + "_" + "thread_" + Thread.currentThread().getId()
                + "_" + deviceName + "_" + "_screenshot.png";

        try {
            FileUtils.copyFile(screenshot, new File(filename));
            Reporter.info("Took a screenshot: [" + filename + "]");
            return filename;
        } catch (IOException e) {
            Reporter.error("Could not take Screenshot: " + e.getMessage());
            return null;
        }
    }

}
