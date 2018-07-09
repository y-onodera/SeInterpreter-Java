package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;

public interface VerticalSurvey {

    RemoteWebDriver getWebDriver();

    default int getFullHeight() {
        return ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight,document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);", new Object[0])).intValue();
    }

    default int getWindowHeight() {
        return ((Number) JavascriptExecutor.class.cast(getWebDriver()).executeScript("return window.innerHeight || document.documentElement.clientHeight || document.getElementsByTagName('body')[0].clientHeight;", new Object[0])).intValue();
    }

}
