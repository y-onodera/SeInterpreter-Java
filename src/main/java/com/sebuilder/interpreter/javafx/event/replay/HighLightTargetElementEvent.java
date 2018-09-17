package com.sebuilder.interpreter.javafx.event.replay;

public class HighLightTargetElementEvent {

    private final String locator;

    private final String value;

    public HighLightTargetElementEvent(String locator, String value) {
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
