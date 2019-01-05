package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;

import java.awt.image.BufferedImage;


public class Page extends AbstractPrintable {
    private int windowHeight;
    private int windowWidth;
    private int scrollableHeight;
    private int viewportHeight;
    private int scrollableWidth;
    private int viewportWidth;

    public Page(TestRun ctx, long scrollTimeout, InnerScrollElementHandler innerScrollElementHandler) {
        super(ctx, scrollTimeout);
        BufferedImage image = getScreenshot();
        this.windowHeight = image.getHeight();
        this.windowWidth = image.getWidth();
        this.viewportHeight = this.getWindowHeight();
        this.viewportWidth = this.getWindowWidth();
        this.scrollableHeight = Math.max(this.getFullHeight(), this.windowHeight);
        this.scrollableWidth = Math.max(this.getFullWidth(), this.windowWidth);
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
