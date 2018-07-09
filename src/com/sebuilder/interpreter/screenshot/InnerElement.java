package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;

import java.awt.image.BufferedImage;

public abstract class InnerElement extends AbstractPrintable {

    private final Printable parent;

    private final int pointY;

    private final WebElement element;

    public InnerElement(Printable parentPage, int pointY, WebElement element, int scrollableHeight, int viewportHeight, InnerElementScrollStrategy innerElementScrollStrategy) {
        super(parentPage.getCtx(), parentPage.scrollTimeout(), scrollableHeight, viewportHeight, innerElementScrollStrategy);
        this.parent = parentPage;
        this.pointY = pointY;
        this.element = element;
    }

    public InnerElement(Printable parentPage, int pointY, WebElement element, int scrollableHeight, int viewportHeight) {
        super(parentPage.getCtx(), parentPage.scrollTimeout(), scrollableHeight, viewportHeight);
        this.parent = parentPage;
        this.pointY = pointY;
        this.element = element;
    }

    @Override
    public BufferedImage getScreenshot() {
        return getParent().getScreenshot();
    }

    @Override
    public int getWindowWidth() {
        return getParent().getWindowWidth();
    }

    @Override
    public void appendImage(BufferedImage part) {
        getParent().appendImage(part);
    }

    public int getPointY() {
        return pointY;
    }

    public WebElement getElement() {
        return element;
    }

    protected Printable getParent() {
        return this.parent;
    }

}
