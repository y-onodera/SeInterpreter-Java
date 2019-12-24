package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

public interface DocumentSurvey {

    RemoteWebDriver getWebDriver();

    int getImageHeight();

    int getImageWidth();

    int getWindowHeight();

    int getWindowWidth();

    default int getFullHeight() {
        WebElement body = getWebDriver().findElementByTagName("body");
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-y"), "hidden")) {
            return getWindowHeight();
        }
        return ((Number) ((JavascriptExecutor) getWebDriver()).executeScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight,document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);", new Object[0])).intValue();
    }

    default int getFullWidth() {
        WebElement body = getWebDriver().findElementByTagName("body");
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-x"), "hidden")) {
            return getWindowWidth();
        }
        return ((Number) ((JavascriptExecutor) getWebDriver()).executeScript("return Math.max(document.body.scrollWidth, document.body.offsetWidth,document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth);", new Object[0])).intValue();
    }

    default BufferedImage getScreenshot() {
        try (ByteArrayInputStream imageArrayStream = new ByteArrayInputStream(this.getWebDriver().getScreenshotAs(OutputType.BYTES))) {
            return ImageIO.read(imageArrayStream);
        } catch (IOException var9) {
            throw new RuntimeException("Can not load screenshot data", var9);
        }
    }


}
