package com.sebuilder.interpreter.screenshot;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.TestRun;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.SortedMap;

public abstract class AbstractPrintable implements Printable {
    private final TestRun ctx;
    private final long scrollTimeout;
    private int scrollableHeight;
    private int viewportHeight;
    private SortedMap<Integer, InnerElement> innerScrollableElement;
    private int innerScrollHeight;
    private int printedHeight;

    protected AbstractPrintable(TestRun ctx, long scrollTimeout, InnerElementScrollStrategy innerElementScrollStrategy) {
        this(ctx, scrollTimeout, 0, 0, innerElementScrollStrategy);
    }

    protected AbstractPrintable(TestRun ctx, long scrollTimeout, int scrollableHeight, int viewportHeight, InnerElementScrollStrategy innerElementScrollStrategy) {
        this.ctx = ctx;
        this.scrollTimeout = scrollTimeout;
        if (scrollableHeight == 0) {
            this.scrollableHeight = this.getFullHeight();
        } else {
            this.scrollableHeight = scrollableHeight;
        }
        if (viewportHeight == 0) {
            this.viewportHeight = this.getWindowHeight();
        } else {
            this.viewportHeight = viewportHeight;
        }
        this.innerScrollableElement = innerElementScrollStrategy.printTarget(this);
        this.innerScrollHeight = this.innerScrollableElement
                .values()
                .stream()
                .reduce(0
                        , (sum, element) -> sum + element.getScrollHeight()
                        , (sum1, sum2) -> sum1 + sum2);
    }

    protected AbstractPrintable(TestRun ctx, long scrollTimeout, int scrollableHeight, int viewportHeight) {
        this.ctx = ctx;
        this.scrollTimeout = scrollTimeout;
        this.scrollableHeight = scrollableHeight;
        this.viewportHeight = viewportHeight;
        this.innerScrollableElement = Maps.newTreeMap();
        this.innerScrollHeight = 0;
    }

    @Override
    public long scrollTimeout() {
        return this.scrollTimeout;
    }

    @Override
    public TestRun getCtx() {
        return ctx;
    }

    @Override
    public int getViewportHeight() {
        return viewportHeight;
    }

    @Override
    public int getScrollableHeight() {
        return scrollableHeight;
    }

    @Override
    public int getInnerScrollHeight() {
        return innerScrollHeight;
    }

    @Override
    public int getPrintedHeight() {
        return printedHeight;
    }

    @Override
    public void setPrintedHeight(int printedHeight) {
        this.printedHeight = printedHeight;
    }

    @Override
    public int nextPrintableHeight(int remainViewPortHeight) {
        return Math.min(remainViewPortHeight, this.getScrollableHeight() - this.getPrintedHeight());
    }

    @Override
    public int printInnerScrollElement(int printFrom) {
        int viewportPrint = 0;
        for (Map.Entry<Integer, InnerElement> target : innerScrollableElement.entrySet()) {
            InnerElement scrollElement = target.getValue();
            int pointY = target.getKey();
            if (this.getPrintedHeight() < pointY && pointY < this.getPrintedHeight() + this.getViewportHeight()) {
                BufferedImage part = this.getScreenshot(printFrom, pointY - this.getPrintedHeight());
                int notScrolled = this.appendImageAndScroll(part);
                if (notScrolled == 0) {
                    scrollElement.printImage(printFrom);
                } else {
                    viewportPrint = notScrolled;
                    scrollElement.printImage(notScrolled);
                }

                if (this.isScrollable(scrollElement.getViewportHeight())) {
                    this.scrollOutPrintedPart(scrollElement.getViewportHeight());
                    viewportPrint = 0;
                } else {
                    appendPrintedHeight(scrollElement.getViewportHeight());
                    if (viewportPrint + scrollElement.getViewportHeight() > this.getViewportHeight()) {
                        this.scrollVertically(this.getScrollableHeight() - this.getViewportHeight());
                        viewportPrint = this.getViewportHeight() - (this.getScrollableHeight() - pointY - scrollElement.getViewportHeight());
                    } else {
                        viewportPrint = viewportPrint + scrollElement.getViewportHeight();
                    }
                }
            }
        }
        return viewportPrint;
    }

    @Override
    public int scrollOutPrintedPart(int printedHeight) {
        appendPrintedHeight(printedHeight);
        if (this.isMoveScrollTopTo(this.getPrintedHeight())) {
            this.scrollVertically(this.getPrintedHeight());
            return 0;
        }
        this.scrollVertically(this.getScrollableHeight() - this.getViewportHeight());
        return this.getPrintedHeight() - (this.getScrollableHeight() - this.getViewportHeight());
    }

    protected boolean isMoveScrollTopTo(int aPointY) {
        return aPointY + this.getViewportHeight() < this.getScrollableHeight();
    }

    protected boolean isScrollable(int aHeight) {
        return this.isMoveScrollTopTo(this.getPrintedHeight() + aHeight);
    }

    protected void resetPrintedHeight() {
        this.setPrintedHeight(0);
    }

    protected void appendPrintedHeight(int printedHeight) {
        this.setPrintedHeight(this.getPrintedHeight() + printedHeight);
    }

}
