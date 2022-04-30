package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.WebDriverWrapper;
import org.openqa.selenium.WebElement;

import java.util.Objects;

public interface DocumentSurvey extends WebDriverWrapper {

    int getImageHeight();

    int getImageWidth();

    int getWindowHeight();

    int getWindowWidth();

    default int getFullHeight() {
        WebElement body = this.getBody();
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-y"), "hidden")) {
            return this.getWindowHeight();
        }
        return ((Number) executeScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight,document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);")).intValue();
    }

    default int getFullWidth() {
        WebElement body = getBody();
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-x"), "hidden")) {
            return getWindowWidth();
        }
        return ((Number) executeScript("return Math.max(document.body.scrollWidth, document.body.offsetWidth,document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth);")).intValue();
    }

}
