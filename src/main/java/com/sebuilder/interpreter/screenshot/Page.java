package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.JavascriptExecutor;

import java.awt.image.BufferedImage;


public class Page extends AbstractPrintable {
    private int imageHeight;
    private int imageWidth;
    private int windowHeight;
    private int windowWidth;
    private int scrollableHeight;
    private int viewportHeight;
    private int scrollableWidth;
    private int viewportWidth;

    public Page(TestRun ctx, long scrollTimeout, InnerScrollElementHandler innerScrollElementHandler) {
        super(ctx, scrollTimeout);
        BufferedImage image = getScreenshot();
        this.imageHeight = image.getHeight();
        this.imageWidth = image.getWidth();
        this.windowHeight = ((Number) ((JavascriptExecutor) getWebDriver()).executeScript("return window.innerHeight || document.documentElement.clientHeight || document.getElementsByTagName('body')[0].clientHeight;", new Object[0])).intValue();
        this.windowWidth = ((Number) ((JavascriptExecutor) getWebDriver()).executeScript("return window.innerWidth || document.documentElement.clientWidth || document.getElementsByTagName('body')[0].clientWidth;", new Object[0])).intValue();
        this.viewportHeight = this.getWindowHeight();
        this.viewportWidth = this.getWindowWidth();
        this.scrollableHeight = this.getFullHeight();
        this.scrollableWidth = this.getFullWidth();
        this.handleInnerScrollElement(innerScrollElementHandler);
    }

    @Override
    public int getPointY() {
        return 0;
    }

    @Override
    public int getPointX() {
        return 0;
    }

    @Override
    public int getImageHeight() {
        return this.imageHeight;
    }

    @Override
    public int getImageWidth() {
        return this.imageWidth;
    }

    @Override
    public int getWindowHeight() {
        return this.windowHeight;
    }

    @Override
    public int getWindowWidth() {
        return this.windowWidth;
    }

    @Override
    public int getViewportHeight() {
        return viewportHeight;
    }

    @Override
    public int getViewportWidth() {
        return viewportWidth;
    }

    @Override
    public int getScrollableHeight() {
        return scrollableHeight;
    }

    @Override
    public int getScrollableWidth() {
        return scrollableWidth;
    }

}
