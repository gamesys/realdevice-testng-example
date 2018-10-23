package com.qa.pageobjects;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.By;

public class RegisterPage extends BasePage {


    //define page elements
    private By emailField = By.id("user_email");
    private By userNameField = By.id("user_login");
    private By passwordField = By.id("user_password");
    private By signUpButton = By.id("signup_button");

    public RegisterPage(AppiumDriver appiumDriver) {
        super(appiumDriver);
    }

    public void waitforPageLoaded() {
        waitForElementToBePresent(userNameField);
    }

    public void populateEmailField(String email) {
        populateElementWithText(emailField, email);
    }

    public void selectEmailField() {
        clickBy(emailField);
        waitForElementToBeSelected(emailField);
    }

    public void populateUserNameField(String name) {
        populateElementWithText(userNameField, name);
    }

    public void selectUserNameField() {
        clickBy(userNameField);
        waitForElementToBeSelected(userNameField);
    }

    public void populatePasswordField(String password) {
        populateElementWithText(passwordField, password);
    }

    public void selectPasswordField() {
        clickBy(passwordField);
        waitForElementToBeSelected(passwordField);
    }

    public String getEmailField() {
        return appiumDriver.findElement(emailField).getAttribute("value");
    }

    public String getUserNameField() {
        return appiumDriver.findElement(userNameField).getAttribute("value");
    }

    public boolean isSignUpButtonEnabled() {
        return appiumDriver.findElement(signUpButton).isEnabled();
    }
}
