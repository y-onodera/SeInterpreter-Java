package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;

import java.awt.image.BufferedImage;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public abstract class AbstractPrintable implements Printable, VerticalSurvey {
    private final TestRun ctx;
    private final long scrollTimeout;
    private SortedMap<Integer, InnerElement> innerScrollableElement;
    private int innerScrollHeight;
    private int innerScrollWidth;

    protected AbstractPrintable(TestRun ctx, long scrollTimeout) {
        this.ctx = ctx;
        this.scrollTimeout = scrollTimeout;
    }

    @Override
    public BufferedImage getScreenshot(int printFrom, int remainHeight, int viewportHeight) {
        BufferedImage part = this.printImage(new HorizontalPrinter());
        return getScreenshot(this.convertImagePerspective(printFrom)
                , this.convertImagePerspective(remainHeight)
                , this.convertImagePerspective(viewportHeight)
                , part);
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
    public int getInnerScrollHeight() {
        return innerScrollHeight;
    }

    @Override
    public int getInnerScrollWidth() {
        return innerScrollWidth;
    }

    @Override
    public Map<Integer, InnerElement> getInnerVerticalScrollableElement() {
        return this.innerScrollableElement
                .entrySet()
                .stream()
                .filter(it -> it.getValue().hasVerticalScroll())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<Integer, InnerElement> getInnerHorizontalScrollableElement() {
        return this.innerScrollableElement
                .entrySet()
                .stream()
                .filter(it -> it.getValue().hasHorizontalScroll())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public Map<Integer, InnerElement> getInnerScrollableElement() {
        return this.innerScrollableElement;
    }

    protected void handleInnerScrollElement(InnerScrollElementHandler innerScrollElementHandler) {
        this.innerScrollableElement = innerScrollElementHandler.handleTarget(this);
        this.innerScrollHeight = this.getInnerVerticalScrollableElement()
                .values()
                .stream()
                .reduce(0
                        , (sum, element) -> sum + element.getScrollHeight() + element.getInnerScrollHeight()
                        , (sum1, sum2) -> sum1 + sum2);
        this.innerScrollWidth = this.getInnerHorizontalScrollableElement()
                .values()
                .stream()
                .reduce(0
                        , (sum, element) -> sum + element.getScrollWidth() + element.getInnerScrollWidth()
                        , (sum1, sum2) -> sum1 + sum2);
    }

    protected BufferedImage getScreenshot(int printFrom, int remainHeight, int viewportHeight, BufferedImage part) {
        int height = Math.min(part.getHeight(), viewportHeight);
        int width = part.getWidth();
        if (remainHeight < height) {
            if (printFrom + viewportHeight < part.getHeight()) {
                part = getSubImage(printFrom, remainHeight, part, width);
            } else {
                if (printFrom + remainHeight < part.getHeight()) {
                    part = getSubImage(printFrom, remainHeight, part, width);
                } else if (printFrom < part.getHeight()) {
                    part = getRestImage(printFrom, part, width);
                }
            }
        } else {
            if (printFrom + height < part.getHeight()) {
                part = getSubImage(printFrom, height, part, width);
            } else if (printFrom < part.getHeight()) {
                part = getRestImage(printFrom, part, width);
            }
        }
        return part;
    }

    protected BufferedImage getSubImage(int printFrom, int remainHeight, BufferedImage part, int width) {
        return part.getSubimage(0, printFrom, width, remainHeight);
    }

    protected BufferedImage getRestImage(int printFrom, BufferedImage part, int width) {
        return part.getSubimage(0, printFrom, width, part.getHeight() - printFrom);
    }

}
