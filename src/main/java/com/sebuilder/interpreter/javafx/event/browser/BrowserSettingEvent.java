package com.sebuilder.interpreter.javafx.event.browser;

public class BrowserSettingEvent {

    private final String selectedBrowser;

    private final String driverPath;

    public BrowserSettingEvent(String selectedBrowser, String driverPath) {
        this.selectedBrowser = selectedBrowser;
        this.driverPath = driverPath;
    }

    public String getSelectedBrowser() {
        return selectedBrowser;
    }

    public String getDriverPath() {
        return driverPath;
    }
}
