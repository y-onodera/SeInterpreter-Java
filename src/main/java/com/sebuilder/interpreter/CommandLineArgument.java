package com.sebuilder.interpreter;

public enum CommandLineArgument {

    IMPLICITLY_WAIT("--implicitlyWait"),
    PAGE_LOAD_TIMEOUT("--pageLoadTimeout"),
    STEP_TYPE_PACKAGE("--stepTypePackage"),
    DRIVER("--driver"),
    DRIVER_CONFIG_PREFIX("--driver."),
    DATASOURCE_ENCODING("--datasource.encoding"),
    DATASOURCE_DIRECTORY("--datasource.directory"),
    SCREENSHOT_OUTPUT("--screenshotoutput"),
    TEMPLATE_OUTPUT("--templateoutput"),
    RESULT_OUTPUT("--resultoutput"),
    DOWNLOAD_OUTPUT("--downloadoutput"),;

    private String key;

    CommandLineArgument(String aKey) {
        this.key = aKey;
    }

    public String key() {
        return this.key;
    }

    public String getArgument(String aValue) {
        return this.key + "=" + aValue;
    }
}
