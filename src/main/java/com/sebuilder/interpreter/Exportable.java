package com.sebuilder.interpreter;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

public interface Exportable {

    default String getTypeName() {
        String simpleClassName = this.getClass().getSimpleName();
        return simpleClassName.substring(0, 1).toLowerCase() + simpleClassName.substring(1);
    }

    default boolean hasLocator() {
        return true;
    }

    default void addElement(ExportResource.Builder builder, RemoteWebDriver driver, WebElement element) {
        // non defalut implementation
    }

}
