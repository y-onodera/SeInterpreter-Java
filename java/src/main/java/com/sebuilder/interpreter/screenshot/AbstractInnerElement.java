package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;

public abstract class AbstractInnerElement extends AbstractPrintable implements InnerElement {

    private final Printable parent;
    private final WebElement element;
    private final ScrollableHeight height;
    private final ScrollableWidth width;

    public AbstractInnerElement(final Printable parentPage
            , final WebElement element
            , final InnerScrollElementHandler innerScrollElementHandler
            , final ScrollableHeight height
            , final ScrollableWidth width
    ) {
        super(parentPage.getCtx());
        this.parent = parentPage;
        this.element = element;
        this.height = height;
        this.width = width;
        this.handleInnerScrollElement(innerScrollElementHandler);
    }

    @Override
    public ScrollableHeight getHeight() {
        return this.height;
    }

    @Override
    public ScrollableWidth getWidth() {
        return this.width;
    }

    @Override
    public WebElement getElement() {
        return this.element;
    }

    @Override
    public Printable getParent() {
        return this.parent;
    }
}
