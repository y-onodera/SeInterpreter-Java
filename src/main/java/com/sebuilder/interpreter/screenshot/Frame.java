package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.awt.image.BufferedImage;

public class Frame extends InnerElement {

    public Frame(Printable parentPage
            , WebElement element
            , LocatorInnerScrollElementHandler innerElementScrollStrategy
            , int pointY
            , int scrollableHeight
            , int viewportHeight
            , int pointX
            , int scrollableWidth
            , int viewportWidth
    ) {
        super(parentPage
                , element
                , innerElementScrollStrategy
                , pointY
                , scrollableHeight
                , viewportHeight
                , pointX
                , scrollableWidth
                , viewportWidth);
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

}
