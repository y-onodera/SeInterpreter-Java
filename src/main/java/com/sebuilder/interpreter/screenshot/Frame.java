package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.awt.image.BufferedImage;

public class Frame extends InnerElement {

    public Frame(Printable parentPage, int pointY, WebElement element, int scrollableHeight, int viewportHeight, LocatorInnerScrollElementHandler innerElementScrollStrategy) {
        super(parentPage, pointY, element, scrollableHeight, viewportHeight, innerElementScrollStrategy);
    }

    @Override
    public BufferedImage getScreenshot() {
        WebDriver wd = getWebDriver();
        wd.switchTo().parentFrame();
        BufferedImage result = super.getScreenshot();
        wd.switchTo().frame(this.getElement());
        return result;
    }

    @Override
    public void printImage(int fromPointY) {
        WebDriver wd = getWebDriver();
        wd.switchTo().frame(this.getElement());
        super.printImage(fromPointY);
        wd.switchTo().parentFrame();
    }

    @Override
    public void setUpPrint(int fromPointY) {
        this.scrollVertically(0);
        this.resetPrintedHeight();
    }
}
