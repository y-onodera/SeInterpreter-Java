package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;

public class ScrollableTag extends InnerElement {

    public ScrollableTag(Printable parentPage, int pointY, WebElement element, int scrollableHeight, int viewportHeight) {
        super(parentPage, pointY, element, scrollableHeight, viewportHeight,InnerScrollElementHandler.ignoreInnerScroll);
    }

    @Override
    public void setUpPrint(int fromPointY) {
        this.scrollVertically(0);
        this.resetPrintedHeight();
    }

    @Override
    public void scrollVertically(int scrollY) {
        this.getWebDriver().executeScript("arguments[0].scrollTop = arguments[1]; return [];", this.getElement(), scrollY);
    }
}
