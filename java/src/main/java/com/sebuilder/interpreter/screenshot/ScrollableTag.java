package com.sebuilder.interpreter.screenshot;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

public class ScrollableTag extends AbstractInnerElement {

    public ScrollableTag(final Printable parentPage
            , final WebElement element
            , final ScrollableHeight height
            , final ScrollableWidth width) {
        super(parentPage, element, InnerScrollElementHandler.ignoreInnerScroll, height, width);
    }

    @Override
    public int scrollOutVertically(final int printedHeight, final int scrolledHeight) {
        if (this.getScrollableHeight() < scrolledHeight && this.getParent().hasVerticalScroll()) {
            return this.getParent().scrollOutVertically(printedHeight, this.getPointY() + scrolledHeight - this.getScrollHeight());
        }
        final int couldNotScroll = super.scrollOutVertically(printedHeight, scrolledHeight);
        if (this.getScrollableHeight() < printedHeight && this.getParent().hasVerticalScroll()) {
            return this.getParent().scrollOutVertically(couldNotScroll, this.getPointY() + scrolledHeight);
        }
        return couldNotScroll;
    }

    public static ScrollableHeight getHeight(final TestRun testRun, final WebElement targetDiv) {
        final Point framePoint = targetDiv.getLocation();
        int pointY = framePoint.getY();
        int viewportHeight = Integer.parseInt(targetDiv.getDomAttribute("clientHeight"));
        int scrollableHeight = Integer.parseInt(targetDiv.getDomAttribute("scrollHeight"));

        final int paddingTop = ((Number) testRun.executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-top') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
        final int paddingBottom = ((Number) testRun.executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-bottom') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
        pointY = pointY + paddingTop;
        viewportHeight = viewportHeight - paddingBottom - paddingTop;
        scrollableHeight = scrollableHeight - paddingBottom - paddingTop;
        return new ScrollableHeight.Builder()
                .setWebDriver(testRun.driver())
                .setTargetElement(targetDiv)
                .setPointY(pointY)
                .setViewportHeight(viewportHeight)
                .setScrollableHeight(scrollableHeight)
                .build();
    }

    public static ScrollableWidth getWidth(final TestRun testRun, final WebElement targetDiv) {
        final Point framePoint = targetDiv.getLocation();
        int pointX = framePoint.getX();
        int viewportWidth = Integer.parseInt(targetDiv.getDomAttribute("clientWidth"));
        int scrollableWidth = Integer.parseInt(targetDiv.getDomAttribute("scrollWidth"));

        final int paddingLeft = ((Number) testRun.executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-left') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
        final int paddingRight = ((Number) testRun.executeScript("return parseInt(document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding-right') || document.defaultView.getComputedStyle(arguments[0],null).getPropertyValue('padding'));", targetDiv)).intValue();
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
