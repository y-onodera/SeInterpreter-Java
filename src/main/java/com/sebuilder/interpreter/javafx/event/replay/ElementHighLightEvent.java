package com.sebuilder.interpreter.javafx.event.replay;

public class ElementHighLightEvent {

    private final String locator;

    private final String value;

    public ElementHighLightEvent(String locator, String value) {
        this.locator = locator;
        this.value = value;
    }

    public String getLocator() {
        return locator;
    }

    public String getValue() {
        return value;
    }
}
