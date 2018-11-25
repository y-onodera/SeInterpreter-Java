package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.Map;

public interface VerticalSurvey extends DocumentSurvey, Scrollable {

    int getPointY();

    int getViewportHeight();

    int getScrollableHeight();

    int getInnerScrollHeight();

    default int getScrollHeight() {
        return getScrollableHeight() - getViewportHeight();
    }

    default boolean hasVerticalScroll() {
        return this.getScrollableHeight() > this.getViewportHeight();
    }

    default boolean isMoveScrollTopTo(int aPointY) {
        return aPointY + this.getViewportHeight() < this.getScrollableHeight();
    }

    Map<Integer, InnerElement> getInnerVerticalScrollableElement();

    default void scrollVertically(int scrollY) {
        if (this.hasVerticalScroll()) {
            JavascriptExecutor.class.cast(getWebDriver()).executeScript("scrollTo(0, arguments[0]); return [];", scrollY);
            waitForScrolling();
        }
    }

    default void scrollVertically(int scrollY, WebElement element) {
        this.getWebDriver().executeScript("arguments[0].scrollTop = arguments[1]; return [];", element, scrollY);
        waitForScrolling();
    }

    default int scrollOutVertically(int printedHeight, int scrolledHeight) {
        int nextScrollTop = printedHeight + scrolledHeight;
        if (this.isMoveScrollTopTo(nextScrollTop)) {
            this.scrollVertically(nextScrollTop);
            return 0;
        }
        if (this.getViewportHeight() >= this.getScrollableHeight()) {
            return nextScrollTop;
        }
        final int scrollY = this.getScrollableHeight() - this.getViewportHeight();
        this.scrollVertically(scrollY);
        return nextScrollTop - scrollY;
    }

    default int nextPrintableHeight(int remainViewPortHeight, int printedHeight) {
        return Math.min(remainViewPortHeight, this.getScrollableHeight() - printedHeight);
    }
}
