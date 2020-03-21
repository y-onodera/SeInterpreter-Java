package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

public class ScrollableTag extends AbstractInnerElement {

    public ScrollableTag(Printable parentPage
            , WebElement element
            , ScrollableHeight height
            , ScrollableWidth width) {
        super(parentPage, element, InnerScrollElementHandler.ignoreInnerScroll, height, width);
    }

    @Override
    public int scrollOutVertically(int printedHeight, int scrolledHeight) {
        if (this.getScrollableHeight() < scrolledHeight && this.getParent().hasVerticalScroll()) {
            return this.getParent().scrollOutVertically(printedHeight, this.getPointY() + scrolledHeight - this.getScrollHeight());
        }
        int couldNotScroll = super.scrollOutVertically(printedHeight, scrolledHeight);
        if (this.getScrollableHeight() < printedHeight && this.getParent().hasVerticalScroll()) {
            return this.getParent().scrollOutVertically(couldNotScroll, this.getPointY() + scrolledHeight);
        }
        return couldNotScroll;
    }

    public static ScrollableHeight getHeight(TestRun testRun, WebElement targetDiv) {
        Point framePoint = targetDiv.getLocation();
        int pointY = framePoint.getY();
        int viewportHeight = Integer.parseInt(targetDiv.getAttribute("clientHeight"));
        int scrollableHeight = Integer.parseInt(targetDiv.getAttribute("scrollHeight"));

        int paddingTop = ((Number) ((JavascriptExecutor) testRun.driver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-top') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
        int paddingBottom = ((Number) ((JavascriptExecutor) testRun.driver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-bottom') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
        pointY = pointY + paddingTop;
        viewportHeight = viewportHeight - paddingBottom - paddingTop;
        scrollableHeight = scrollableHeight  - paddingBottom - paddingTop;
        return new ScrollableHeight.Builder()
                .setWebDriver(testRun.driver())
                .setTargetElement(targetDiv)
                .setPointY(pointY)
                .setViewportHeight(viewportHeight)
                .setScrollableHeight(scrollableHeight)
                .build();
    }

    public static ScrollableWidth getWidth(TestRun testRun, WebElement targetDiv) {
        Point framePoint = targetDiv.getLocation();
        int pointX = framePoint.getX();
        int viewportWidth = Integer.parseInt(targetDiv.getAttribute("clientWidth"));
        int scrollableWidth = Integer.parseInt(targetDiv.getAttribute("scrollWidth"));

        int paddingLeft = ((Number) ((JavascriptExecutor) testRun.driver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-left') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
        int paddingRight = ((Number) ((JavascriptExecutor) testRun.driver()).executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-right') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
        pointX = pointX + paddingLeft;
        viewportWidth = viewportWidth - paddingRight - paddingLeft;
        scrollableWidth = scrollableWidth - paddingRight - paddingLeft;
        return new ScrollableWidth.Builder()
                .setWebDriver(testRun.driver())
                .setTargetElement(targetDiv)
                .setPointX(pointX)
                .setViewportWidth(viewportWidth)
                .setScrollableWidth(scrollableWidth)
                .build();
    }
}
