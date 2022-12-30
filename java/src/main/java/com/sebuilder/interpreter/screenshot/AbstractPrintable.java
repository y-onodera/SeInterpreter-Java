package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;

import java.util.Map;
import java.util.SortedMap;
import java.util.stream.Collectors;

public abstract class AbstractPrintable implements Printable {

    private final TestRun ctx;
    private SortedMap<Integer, InnerElement> innerScrollableElement;
    private int innerScrollHeight;
    private int innerScrollWidth;

    protected AbstractPrintable(final TestRun ctx) {
        this.ctx = ctx;
    }

    @Override
    public TestRun getCtx() {
        return this.ctx;
    }

    @Override
    public int getInnerScrollHeight() {
        return this.innerScrollHeight;
    }

    @Override
    public int getInnerScrollWidth() {
        return this.innerScrollWidth;
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

    protected void handleInnerScrollElement(final InnerScrollElementHandler innerScrollElementHandler) {
        this.innerScrollableElement = innerScrollElementHandler.handleTarget(this);
        this.innerScrollHeight = this.getInnerVerticalScrollableElement()
                .values()
                .stream()
                .reduce(0
                        , (sum, element) -> sum + element.getScrollHeight() + element.getInnerScrollHeight()
                        , Integer::sum);
        this.innerScrollWidth = this.getInnerHorizontalScrollableElement()
                .values()
                .stream()
                .reduce(0
                        , (sum, element) -> sum + element.getScrollWidth() + element.getInnerScrollWidth()
                        , Integer::sum);
    }

}
