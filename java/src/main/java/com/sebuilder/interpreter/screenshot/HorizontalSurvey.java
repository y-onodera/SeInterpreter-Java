package com.sebuilder.interpreter.screenshot;

import java.util.Map;

public interface HorizontalSurvey extends DocumentSurvey {

    ScrollableWidth getWidth();

    default int getPointX() {
        return this.getWidth().getPointX();
    }

    default int getViewportWidth() {
        return this.getWidth().getViewportWidth();
    }

    default int getScrollableWidth() {
        return this.getWidth().getScrollableWidth();
    }

    default int getScrollWidth() {
        return this.getWidth().getScrollWidth();
    }

    default boolean hasHorizontalScroll() {
        return this.getWidth().hasHorizontalScroll();
    }

    default void scrollHorizontally(final int scrollX) {
        this.getWidth().scrollHorizontally(scrollX);
    }

    default int scrollOutHorizontally(final int printedWidth) {
        return this.getWidth().scrollOutHorizontally(printedWidth);
    }

    int getInnerScrollWidth();

    default int getFullImageWidth() {
        return this.convertImageWidth(this.getWindowWidth() + this.getScrollWidth() + this.getInnerScrollWidth());
    }

    Map<Integer, InnerElement> getInnerHorizontalScrollableElement();

    default int convertImageWidth(final int documentWidth) {
        return documentWidth * this.getImageWidth() / this.getWindowWidth();
    }

    default int convertDocumentWidth(final int imageWidth) {
        return imageWidth * this.getWindowWidth() / this.getImageWidth();
    }
}
