package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;

import java.awt.image.BufferedImage;


public class Page extends AbstractPrintable {
    private final int imageHeight;
    private final int imageWidth;
    private final int windowHeight;
    private final int windowWidth;
    private final ScrollableHeight height;
    private final ScrollableWidth width;

    public Page(TestRun ctx, InnerScrollElementHandler innerScrollElementHandler) {
        super(ctx);
        BufferedImage image = getScreenshot();
        this.height = getHeight(ctx);
        this.width = getWidth(ctx);
        this.imageHeight = image.getHeight();
        this.imageWidth = image.getWidth();
        this.windowHeight = ctx.getWindowHeight();
        this.windowWidth = ctx.getWindowWidth();
        this.handleInnerScrollElement(innerScrollElementHandler);
    }

    public static ScrollableWidth getWidth(TestRun ctx) {
        return new ScrollableWidth.Builder()
                .setWebDriver(ctx.driver())
                .setViewportWidth(ctx.getClientWidth())
                .setScrollableWidth(ctx.getContentWidth())
                .build();
    }

    public static  ScrollableHeight getHeight(TestRun ctx) {
        return new ScrollableHeight.Builder()
                .setWebDriver(ctx.driver())
                .setViewportHeight(ctx.getClientHeight())
                .setScrollableHeight(ctx.getContentHeight())
                .build();
    }

    @Override
    public ScrollableWidth getWidth() {
        return this.width;
    }

    @Override
    public ScrollableHeight getHeight() {
        return this.height;
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

}
