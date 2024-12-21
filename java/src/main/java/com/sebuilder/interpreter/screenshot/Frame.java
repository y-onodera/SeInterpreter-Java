package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.image.BufferedImage;

public class Frame extends AbstractInnerElement {

    public Frame(final Printable parentPage
            , final WebElement element
            , final LocatorInnerScrollElementHandler innerElementScrollStrategy
            , final ScrollableHeight height
            , final ScrollableWidth width) {
        super(parentPage, element, innerElementScrollStrategy, height, width);
    }

    @Override
    public BufferedImage printImage(final VerticalPrinter aPrinter, final int fromPointY) {
        final WebDriver wd = this.driver();
        wd.switchTo().frame(this.getElement());
        final BufferedImage result = super.printImage(aPrinter, fromPointY);
        wd.switchTo().parentFrame();
        return result;
    }

    @Override
    public BufferedImage getScreenshot() {
        final WebDriver wd = this.driver();
        wd.switchTo().parentFrame();
        final BufferedImage result = this.getParent().getScreenshot();
        wd.switchTo().frame(this.getElement());
        return result;
    }

    public static ScrollableHeight getHeight(final Printable parent, final RemoteWebDriver wd, final WebElement targetFrame) {
        final int border = ((Number) parent.executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-top-width'));", targetFrame)).intValue();
        final int pointY = getPointY(targetFrame, border, wd);
        final int viewportHeight = getClientHeight(targetFrame, border, wd);
        wd.switchTo().frame(targetFrame);
        final int scrollableHeight = getScrollHeight(parent.getFullHeight(), border, wd);
        wd.switchTo().parentFrame();
        return new ScrollableHeight.Builder().setWebDriver(wd)
                .setPointY(pointY)
                .setViewportHeight(viewportHeight)
                .setScrollableHeight(scrollableHeight)
                .build();
    }

    public static ScrollableWidth getWidth(final Printable parent, final RemoteWebDriver wd, final WebElement targetFrame) {
        final int borderWidth = ((Number) parent.executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('border-left-width'));", targetFrame)).intValue();
        final int pointX = targetFrame.getLocation().getX() + borderWidth;
        final int viewportWidth = Integer.parseInt(targetFrame.getDomAttribute("clientWidth"));
        wd.switchTo().frame(targetFrame);
        final int scrollableWidth = parent.getFullWidth();
        wd.switchTo().parentFrame();
        return new ScrollableWidth.Builder().setWebDriver(wd)
                .setPointX(pointX)
                .setViewportWidth(viewportWidth)
                .setScrollableWidth(scrollableWidth)
                .build();
    }

    protected static int getPointY(final WebElement target, final int border, final RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return target.getLocation().getY() + border * 2;
        }
        return target.getLocation().getY() + border;
    }

    protected static int getClientHeight(final WebElement target, final int border, final RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return Integer.parseInt(target.getDomAttribute("clientHeight")) - border;
        }
        return Integer.parseInt(target.getDomAttribute("clientHeight"));
    }

    protected static int getScrollHeight(final int scrollHeight, final int border, final RemoteWebDriver wd) {
        if (wd instanceof FirefoxDriver || wd instanceof InternetExplorerDriver) {
            return scrollHeight - border;
        }
        return scrollHeight;
    }

}
