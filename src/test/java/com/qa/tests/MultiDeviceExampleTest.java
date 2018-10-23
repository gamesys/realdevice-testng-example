package com.qa.tests;

import com.qa.basetest.MultiDeviceBaseTest;
import com.qa.basetest.TestTags;
import com.qa.basetest.tags.DeviceType;
import com.qa.basetest.tags.Phase;
import com.qa.basetest.tags.Platform;
import com.qa.pageobjects.HomePage;
import com.qa.pageobjects.RegisterPage;
import com.qa.utils.Reporter;
import com.qa.utils.TestUtils;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.HashMap;

public class MultiDeviceExampleTest extends MultiDeviceBaseTest {

    @Factory(dataProvider = "getDevices")
    public MultiDeviceExampleTest(HashMap<String, Object> deviceConfig) {
        super(deviceConfig);
    }

    @TestTags(phase = Phase.REGRESSION, devicetype = DeviceType.PHONE, platform = {Platform.ANDROID, Platform.IOS}, tags="github")
    @Test
    public void exampleAppiumTest() {
        //visit home page
        HomePage homePage = new HomePage(appiumDriver, baseProperties);
        homePage.navigateTo();
        homePage.toggleMenu();

        //go to registration page.
        RegisterPage registerPage = homePage.clickSignUpButton();
        registerPage.waitforPageLoaded();
        TestUtils.takeScreenshot(appiumDriver);
        Reporter.info("Current Url : " + appiumDriver.getCurrentUrl());

        //populate the fields
        registerPage.selectUserNameField();
        registerPage.populateUserNameField("username1234");
        registerPage.selectEmailField();
        registerPage.populateEmailField("user123@emailaddress.com");
        registerPage.selectPasswordField();
        registerPage.populatePasswordField("MyP@ssw0rd1s5up3rSecur3");

        //assert the values
        Assert.assertEquals(registerPage.getUserNameField(), "username1234");
        Assert.assertEquals(registerPage.getEmailField(), "user123@emailaddress.com");
        Assert.assertEquals(registerPage.isSignUpButtonEnabled(), true);

        TestUtils.takeScreenshot(appiumDriver);
    }

    @AfterClass
    public void afterClass() {
        appiumDriver.close();
        appiumDriver.quit();
    }

}
