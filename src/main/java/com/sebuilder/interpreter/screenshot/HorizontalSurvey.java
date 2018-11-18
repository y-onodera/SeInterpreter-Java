package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import java.util.Map;

public interface HorizontalSurvey extends DocumentSurvey, Scrollable {

    int getViewportWidth();

    int getScrollableWidth();

    int getInnerScrollWidth();

    default int getScrollWidth() {
        return getScrollableWidth() - getViewportWidth() + getInnerScrollWidth();
    }

    default boolean hasHorizontalScroll() {
        return this.getScrollableWidth() > this.getViewportWidth();
    }

    Map<Integer, InnerElement> getInnerHorizontalScrollableElement();

    default void scrollHorizontally(int scrollY) {
        if (this.hasHorizontalScroll()) {
            JavascriptExecutor.class.cast(getWebDriver()).executeScript("scrollTo(0, arguments[0]); return [];", scrollY);
            waitForScrolling();
        }
    }

    default void scrollHorizontally(int scrollY, WebElement element) {
        this.getWebDriver().executeScript("arguments[0].scrollTop = arguments[1]; return [];", element, scrollY);
        waitForScrolling();
    }

}
