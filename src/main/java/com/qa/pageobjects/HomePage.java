package com.qa.pageobjects;

import com.qa.utils.Reporter;
import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.util.Properties;

public class HomePage extends BasePage {

    private Properties baseProperties;

    //define page elements
    private By toggleMenuButton = By.className("mt-1");
    private By signUpButton = By.className("text-bold");

    public HomePage(AppiumDriver appiumDriver, Properties baseProperties) {
        super(appiumDriver);
        this.baseProperties = baseProperties;
    }

    public void navigateTo() {
        String url = baseProperties.get("url").toString();
        appiumDriver.get(url);
        Reporter.info("Navigated to [" + url + "]");
    }

    public void toggleMenu() {
        clickBy(toggleMenuButton);
    }

    public RegisterPage clickSignUpButton() {
        waitForElementToBeClickable(signUpButton);
        WebElement signupButton = (WebElement) appiumDriver.findElements(signUpButton).get(1);
        signupButton.click();
        return new RegisterPage(appiumDriver);
    }


}
