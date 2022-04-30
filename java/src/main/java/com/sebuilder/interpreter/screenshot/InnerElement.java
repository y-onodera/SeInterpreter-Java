package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;

public interface InnerElement extends Printable {

    WebElement getElement();

    Printable getParent();

    @Override
    default int getImageHeight() {
        return this.getParent().getImageHeight();
    }

    @Override
    default int getWindowHeight() {
        return this.getParent().getWindowHeight();
    }

    @Override
    default int getImageWidth() {
        return this.getParent().getImageWidth();
    }

    @Override
    default int getWindowWidth() {
        return this.getParent().getWindowWidth();
    }

    @Override
    default int getFullImageWidth() {
        return this.getParent().getFullImageWidth();
    }
}
