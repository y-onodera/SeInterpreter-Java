package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;

import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public abstract class AbstractPrintable implements Printable, VerticalSurvey {
    private final TestRun ctx;
    private final long scrollTimeout;
    private SortedMap<Integer, InnerElement> innerScrollableElement;
    private int innerScrollHeight;
    private int innerScrollWidth;

    protected AbstractPrintable(TestRun ctx, long scrollTimeout, InnerScrollElementHandler innerScrollElementHandler) {
        this.ctx = ctx;
        this.scrollTimeout = scrollTimeout;
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
}
