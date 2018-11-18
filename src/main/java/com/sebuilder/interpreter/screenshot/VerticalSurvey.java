package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.Map;

public interface VerticalSurvey extends DocumentSurvey, Scrollable {

    int getViewportHeight();

    int getScrollableHeight();

    int getInnerScrollHeight();

    default int getScrollHeight() {
        return getScrollableHeight() - getViewportHeight() + getInnerScrollHeight();
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

    default int scrollOutVertically(int printedHeight) {
        if (this.isMoveScrollTopTo(printedHeight)) {
            this.scrollVertically(printedHeight);
            return 0;
        }
        if (this.getViewportHeight() >= this.getScrollableHeight()) {
            return printedHeight;
        }
        final int scrollY = this.getScrollableHeight() - this.getViewportHeight();
        this.scrollVertically(scrollY);
        return printedHeight - scrollY;
    }

}
