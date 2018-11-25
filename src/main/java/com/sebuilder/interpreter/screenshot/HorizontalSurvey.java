package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.Map;

public interface HorizontalSurvey extends DocumentSurvey, Scrollable {

    int getPointX();

    int getViewportWidth();

    int getScrollableWidth();

    int getInnerScrollWidth();

    default int getScrollWidth() {
        return getScrollableWidth() - getViewportWidth();
    }

    default boolean hasHorizontalScroll() {
        return this.getScrollableWidth() > this.getViewportWidth();
    }

    default boolean isMoveScrollLeftTo(int aPointX) {
        return aPointX + this.getViewportWidth() < this.getScrollableWidth();
    }

    Map<Integer, InnerElement> getInnerHorizontalScrollableElement();

    default void scrollHorizontally(int scrollX) {
        if (this.hasHorizontalScroll()) {
            JavascriptExecutor.class.cast(getWebDriver()).executeScript("scrollTo(arguments[0] ,0); return [];", scrollX);
            waitForScrolling();
        }
    }

    default void scrollHorizontally(int scrollX, WebElement element) {
        this.getWebDriver().executeScript("arguments[0].scrollLeft = arguments[1]; return [];", element, scrollX);
        waitForScrolling();
    }

    default int scrollOutHorizontally(int printedWidth) {
        if (this.isMoveScrollLeftTo(printedWidth)) {
            this.scrollHorizontally(printedWidth);
            return 0;
        }
        if (this.getViewportWidth() >= this.getScrollableWidth()) {
            return printedWidth;
        }
        final int scrollX = this.getScrollableWidth() - this.getViewportWidth();
        this.scrollHorizontally(scrollX);
        return printedWidth - scrollX;
    }
}
