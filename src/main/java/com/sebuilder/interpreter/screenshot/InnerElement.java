package com.sebuilder.interpreter.screenshot;

import org.openqa.selenium.WebElement;

public interface InnerElement extends Printable {
    WebElement getElement();

    int getPointY();

    Printable getParent();
}
