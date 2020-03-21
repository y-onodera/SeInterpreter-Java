package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.image.BufferedImage;

public class Frame extends AbstractInnerElement {

    public Frame(Printable parentPage
            , WebElement element
            , LocatorInnerScrollElementHandler innerElementScrollStrategy
            , ScrollableHeight height
            , ScrollableWidth width) {
        super(parentPage, element, innerElementScrollStrategy, height, width);
    }

    @Override
    public BufferedImage printImage(VerticalPrinter aPrinter, int fromPointY) {
        WebDriver wd = getWebDriver();
        wd.switchTo().frame(this.getElement());
        BufferedImage result = super.printImage(aPrinter, fromPointY);
        wd.switchTo().parentFrame();
        return result;
    }

    @Override
    public BufferedImage getScreenshot() {
        WebDriver wd = getWebDriver();
        wd.switchTo().parentFrame();
        BufferedImage result = this.getParent().getScreenshot();
        wd.switchTo().frame(this.getElement());
        return result;
    }

    public static ScrollableHeight getHeight(Printable parent, RemoteWebDriver wd, WebElement targetFrame) {
        int border = ((Number) ((JavascriptExecutor) wd).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-top-width'));", targetFrame)).intValue();
        int pointY = getPointY(targetFrame, border, wd);
        int viewportHeight = getClientHeight(targetFrame, border, wd);
        wd.switchTo().frame(targetFrame);
        int scrollableHeight = getScrollHeight(parent.getFullHeight(), border, wd);
        wd.switchTo().parentFrame();
        return new ScrollableHeight.Builder().setWebDriver(wd)
                .setPointY(pointY)
                .setViewportHeight(viewportHeight)
                .setScrollableHeight(scrollableHeight)
                .build();
    }

    public static ScrollableWidth getWidth(Printable parent, RemoteWebDriver wd, WebElement targetFrame) {
        int borderWidth = ((Number) ((JavascriptExecutor) wd).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-left-width'));", targetFrame)).intValue();
        int pointX = targetFrame.getLocation().getX() + borderWidth;
        int viewportWidth = Integer.parseInt(targetFrame.getAttribute("clientWidth"));
        wd.switchTo().frame(targetFrame);
        int scrollableWidth = parent.getFullWidth();
        wd.switchTo().parentFrame();
        return new ScrollableWidth.Builder().setWebDriver(wd)
                .setPointX(pointX)
                .setViewportWidth(viewportWidth)
                .setScrollableWidth(scrollableWidth)
                .build();
    }

    protected static int getPointY(WebElement target, int border, RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return target.getLocation().getY() + border * 2;
        }
        return target.getLocation().getY() + border;
    }

    protected static int getClientHeight(WebElement target, int border, RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return Integer.parseInt(target.getAttribute("clientHeight")) - border;
        }
        return Integer.parseInt(target.getAttribute("clientHeight"));
    }

    protected static int getScrollHeight(int scrollHeight, int border, RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return scrollHeight - border;
        }
        return scrollHeight;
    }

}
