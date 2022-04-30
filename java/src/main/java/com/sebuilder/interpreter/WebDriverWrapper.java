package com.sebuilder.interpreter;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;

public interface WebDriverWrapper {

    RemoteWebDriver driver();

    default String getHtml() {
        return driver().findElement(By.tagName("html")).getText();
    }

    default WebElement getBody() {
        return driver().findElement(By.tagName("body"));
    }

    default int getWindowHeight() {
        return ((Number) this.executeScript("return window.innerHeight;", new Object[0])).intValue();
    }

    default int getWindowWidth() {
        return ((Number) this.executeScript("return window.innerWidth;", new Object[0])).intValue();
    }

    default int getClientHeight() {
        return ((Number) this.executeScript("return document.documentElement.clientHeight;", new Object[0])).intValue();
    }

    default int getClientWidth() {
        return ((Number) this.executeScript("return document.documentElement.clientWidth;", new Object[0])).intValue();
    }

    default int getContentHeight() {
        WebElement body = this.getBody();
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-y"), "hidden")) {
            return getClientHeight();
        }
        return ((Number) this.executeScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight,document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);", new Object[0])).intValue();
    }

    default int getContentWidth() {
        WebElement body = this.getBody();
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-x"), "hidden")) {
            return getClientWidth();
        }
        return ((Number) this.executeScript("return Math.max(document.body.scrollWidth, document.body.offsetWidth,document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth);", new Object[0])).intValue();
    }

    default BufferedImage getScreenshot() {
        try (ByteArrayInputStream imageArrayStream = new ByteArrayInputStream(this.driver().getScreenshotAs(OutputType.BYTES))) {
            return ImageIO.read(imageArrayStream);
        } catch (IOException var9) {
            throw new RuntimeException("Can not load screenshot data", var9);
        }
    }

    default BufferedImage getScreenshot(Locator locator) {
        try (ByteArrayInputStream imageArrayStream = new ByteArrayInputStream(locator.find(this.driver()).getScreenshotAs(OutputType.BYTES))) {
            return ImageIO.read(imageArrayStream);
        } catch (IOException var9) {
            throw new RuntimeException("Can not load screenshot data", var9);
        }
    }

    default Object executeScript(String s) {
        return this.executeScript(s, new Object[0]);
    }

    default Object executeScript(String s, Object... params) {
        return ((JavascriptExecutor) this.driver()).executeScript(s, params);
    }

    default Locator detectLocator(WebElement element) {
        return Locator.of(this, element);
    }
}
