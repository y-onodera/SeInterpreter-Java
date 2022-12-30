package com.sebuilder.interpreter.screenshot;

import java.util.Map;

public interface VerticalSurvey extends DocumentSurvey {

    ScrollableHeight getHeight();

    default int getPointY() {
        return this.getHeight().getPointY();
    }

    default int getViewportHeight() {
        return this.getHeight().getViewportHeight();
    }

    default int getScrollableHeight() {
        return this.getHeight().getScrollableHeight();
    }

    default int getScrollHeight() {
        return this.getHeight().getScrollHeight();
    }

    default boolean hasVerticalScroll() {
        return this.getHeight().hasVerticalScroll();
    }

    default boolean isEnableMoveScrollTopTo(final int aPointY) {
        return this.getHeight().isEnableMoveScrollTopTo(aPointY);
    }

    default void scrollVertically(final int scrollY) {
        this.getHeight().scrollVertically(scrollY);
    }

    default int scrollOutVertically(final int toBeScrollOut, final int scrolledHeight) {
        return this.getHeight().scrollOutVertically(toBeScrollOut, scrolledHeight);
    }

    default int nextPrintableHeight(final int remainViewPortHeight, final int printedHeight) {
        return Math.min(remainViewPortHeight, this.getScrollableHeight() - printedHeight);
    }

    int getInnerScrollHeight();

    default int getFullImageHeight() {
        return this.convertImageHeight(this.getScrollableHeight() + this.getInnerScrollHeight());
    }

    Map<Integer, InnerElement> getInnerVerticalScrollableElement();

    default int convertImageHeight(final int documentHeight) {
        return documentHeight * this.getImageHeight() / this.getWindowHeight();
    }

    default int convertDocumentHeight(final int imageHeight) {
        return imageHeight * this.getWindowHeight() / this.getImageHeight();
    }

}
