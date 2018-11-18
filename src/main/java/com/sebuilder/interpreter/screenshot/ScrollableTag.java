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
    public void scrollVertically(int scrollY) {
        this.scrollVertically(scrollY, this.getElement());
    }
}
