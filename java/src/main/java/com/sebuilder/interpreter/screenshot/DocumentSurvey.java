package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.WebDriverWrapper;
import org.openqa.selenium.WebElement;

import java.util.Objects;

public interface DocumentSurvey extends WebDriverWrapper {

    int getImageHeight();

    int getImageWidth();

    @Override
    int getWindowHeight();

    @Override
    int getWindowWidth();

    default int getFullHeight() {
        final WebElement body = this.getBody();
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-y"), "hidden")) {
            return this.getWindowHeight();
        }
        return ((Number) this.executeScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight,document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);")).intValue();
    }

    default int getFullWidth() {
        final WebElement body = this.getBody();
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-x"), "hidden")) {
            return this.getWindowWidth();
        }
        return ((Number) this.executeScript("return Math.max(document.body.scrollWidth, document.body.offsetWidth,document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth);")).intValue();
    }

}
