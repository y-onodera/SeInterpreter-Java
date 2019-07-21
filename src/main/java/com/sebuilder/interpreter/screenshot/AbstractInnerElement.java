package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;

public abstract class AbstractInnerElement extends AbstractPrintable implements InnerElement {

    private final Printable parent;
    private final WebElement element;
    private final int pointY;
    private int scrollableHeight;
    private int viewportHeight;
    private final int pointX;
    private int scrollableWidth;
    private int viewportWidth;

    public AbstractInnerElement(Printable parentPage
            , WebElement element
            , InnerScrollElementHandler innerScrollElementHandler
            , int pointY
            , int scrollableHeight
            , int viewportHeight
            , int pointX
            , int scrollableWidth
            , int viewportWidth
    ) {
        super(parentPage.getCtx(), parentPage.scrollTimeout());
        this.parent = parentPage;
        this.element = element;
        this.pointY = pointY;
        this.viewportHeight = viewportHeight;
        this.scrollableHeight = scrollableHeight;
        this.pointX = pointX;
        this.viewportWidth = viewportWidth;
        this.scrollableWidth = scrollableWidth;
        this.handleInnerScrollElement(innerScrollElementHandler);
    }

    @Override
    public WebElement getElement() {
        return element;
    }

    @Override
    public int getPointY() {
        return pointY;
    }

    @Override
    public int getImageHeight() {
        return this.getParent().getImageHeight();
    }

    @Override
    public int getWindowHeight() {
        return this.getParent().getWindowHeight();
    }

    @Override
    public int getViewportHeight() {
        return viewportHeight;
    }

    @Override
    public int getScrollableHeight() {
        return scrollableHeight;
    }

    @Override
    public int getPointX() {
        return pointX;
    }

    @Override
    public int getImageWidth() {
        return this.getParent().getImageWidth();
    }

    @Override
    public int getWindowWidth() {
        return this.getParent().getWindowWidth();
    }

    @Override
    public int getViewportWidth() {
        return viewportWidth;
    }

    @Override
    public int getScrollableWidth() {
        return scrollableWidth;
    }

    @Override
    public Printable getParent() {
        return this.parent;
    }

}
