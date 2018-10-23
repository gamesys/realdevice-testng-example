package com.qa.basetest;

import com.qa.utils.ExcelHelper;
import com.qa.utils.PropertiesHelper;
import com.qa.utils.Reporter;
import com.qa.utils.TestUtils;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.apache.log4j.Logger;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.testng.annotations.*;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

@Listeners({ BaseMethodsInterceptor.class })
public class MultiDeviceBaseTest {

    //grab the required test properties
    protected Properties baseProperties = PropertiesHelper.loadPropertiesFile("/test.properties");
    protected Properties desiredCapsProperties = PropertiesHelper.loadPropertiesFile("/devices/desiredcaps.properties");

    protected String deviceName;
    protected String deviceType;
    protected String platformName;
    protected URL sauceLabsURL;

    protected AppiumDriver<WebElement> appiumDriver;

    /**
     *  This data provider can be adapted to return a single or a whole bunch of devices.
     *
     * @return
     */
    @DataProvider(name = "getDevices", parallel = true)
    public static Iterator<Object[]> getDevices() {
        Properties properties = PropertiesHelper.loadPropertiesFile("/test.properties");

        String deviceName = properties.getProperty("saucelabs_device_name");
        String platformName = properties.getProperty("saucelabs_platform_name");
        String platformVersion = properties.getProperty("saucelabs_platform_version");
        Reporter.info("Filtering arguments provided DeviceName [" + deviceName + "], PlatformName [" + platformName + "], PlatformVersion [" + platformVersion + "]");

        ArrayList<Object[]> filterMap = filterDevicesByArguments(deviceName, platformName, platformVersion);

        Reporter.info("Number of device selected from sheet [" + filterMap.size() + "]");
        return filterMap.iterator();
    }

    /**
     * Configure the device details for filtering.
     *
     * @param deviceConfig
     */
    public MultiDeviceBaseTest(HashMap<String, Object> deviceConfig) {
        this.deviceType = deviceConfig.get("deviceType").toString();
        this.deviceName = deviceConfig.get("name").toString();
        this.platformName = deviceConfig.get("platformName").toString();
        String urlString = baseProperties.getProperty("saucelabs_endpoint");

        //grab url from config and created desired caps.
        try {
            sauceLabsURL = new URL(urlString);
        } catch (MalformedURLException e) {
            Reporter.error("The URL provided [" + urlString + "] is invalid due to [" + e.getMessage() + "]");
        }

        //created desired capabilities from 2 sources.
        DesiredCapabilities desiredCapabilities = TestUtils.createDesiredCaps(desiredCapsProperties, deviceConfig);

        //set test name (optional)
        desiredCapabilities.setCapability("testobject_test_name", this.getClass().getSimpleName());

        //instantiate driver.
        if (deviceConfig.get("platformName").toString().equalsIgnoreCase("IOS")) {
            appiumDriver = new IOSDriver<>(sauceLabsURL, desiredCapabilities);
        } else {
            appiumDriver = new AndroidDriver<>(sauceLabsURL, desiredCapabilities);
        }
        //report the device it was run against and the report url.
        Reporter.info("Running tests against device [" + appiumDriver.getCapabilities().getCapability("testobject_device").toString() + "]. Results can be found here: [" + appiumDriver.getCapabilities().getCapability("testobject_test_report_url").toString() + "]");
    }


    @BeforeMethod(alwaysRun = true)
    public synchronized void before(Method method) {
        //setup test.
        Reporter.info("### Starting test [" + method.getName() + "] ###");
    }

    @AfterMethod(alwaysRun = true)
    public synchronized void after(Method method) {
        //teardown test.
        Reporter.info("### Finishing test [" + method.getName() + "] ###");
    }

    public String getDeviceType() {
        return this.deviceType;
    }

    public String getPlatformName() {
        return this.platformName;
    }

    /**
     *
     * @param deviceName
     * @param platformName
     * @param platformVersion
     * @return
     */
    private static ArrayList<Object[]> filterDevicesByArguments(String deviceName, String platformName, String platformVersion) {
        ArrayList<Object[]> filterMap = null;
        ExcelHelper excelHelper = new ExcelHelper();
        ArrayList<Object[]> allDevicesMap = excelHelper.retrieveAllRows("src/test/resources/devices/devicelist.xlsx", "RealDevices");

        //do some filtering here and return a single device (can easily be adapted to return multiple devices).
        if (!deviceName.isEmpty()) {
            if (!deviceName.contains(",")) {
                filterMap = excelHelper.filterMapByColumn(allDevicesMap, "name", deviceName);
            } else {
                filterMap = excelHelper.filterMapByColumn(allDevicesMap, "name", deviceName.split(","));
            }
        } else if (!platformName.isEmpty()) {
            if (platformVersion.isEmpty()) {
                //filter by platform
                filterMap = excelHelper.filterMapByColumn(allDevicesMap, "platformName", platformName);
            } else  {
                // filter by platform and then version
                filterMap = excelHelper.filterMapByColumn(allDevicesMap, "platformName", platformName);
                filterMap = excelHelper.filterMapByColumn(filterMap, "platformVersion", platformVersion);
            }
        } else if (!platformVersion.isEmpty()) {
            //filter by platform Version
            filterMap = excelHelper.filterMapByColumn(allDevicesMap, "platformVersion", platformVersion);
        }

        return filterMap;
    }
}
