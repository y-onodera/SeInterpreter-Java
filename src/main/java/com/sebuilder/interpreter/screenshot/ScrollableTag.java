package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;

public class ScrollableTag extends InnerElement {

    public ScrollableTag(Printable parentPage
            , WebElement element
            , int pointY
            , int scrollableHeight
            , int viewportHeight
            , int pointX
            , int scrollableWidth
            , int viewportWidth
    ) {
        super(parentPage
                , element
                , InnerScrollElementHandler.ignoreInnerScroll
                , pointY
                , scrollableHeight
                , viewportHeight
                , pointX
                , scrollableWidth
                , viewportWidth
        );
    }

    @Override
    public int scrollOutVertically(int printedHeight, int scrolledHeight) {
        if (this.getScrollHeight() <= scrolledHeight) {
            return this.getParent().scrollOutVertically(printedHeight, this.getPointY() + scrolledHeight - this.getScrollHeight());
        }
        int couldNotScroll = super.scrollOutVertically(printedHeight, scrolledHeight);
        if (this.getScrollHeight() < printedHeight) {
            return this.getParent().scrollOutVertically(couldNotScroll, this.getPointY() + scrolledHeight);
        }
        return couldNotScroll;
    }

    @Override
    public void scrollVertically(int scrollY) {
        this.scrollVertically(scrollY, this.getElement());
    }

    @Override
    public void scrollHorizontally(int scrollX) {
        this.scrollHorizontally(scrollX, this.getElement());
    }
}
