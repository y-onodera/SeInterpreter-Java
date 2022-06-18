package com.sebuilder.interpreter.application;

public enum CommandLineArgument {

    IMPLICITLY_WAIT("--implicitlyWait"),
    PAGE_LOAD_TIMEOUT("--pageLoadTimeout"),
    DRIVER("--driver"),
    DRIVER_PATH("--driverPath"),
    DRIVER_CONFIG_PREFIX("--driver."),
    DATASOURCE_ENCODING("--datasource.encoding"),
    DATASOURCE_DIRECTORY("--datasource.directory"),
    RESULT_OUTPUT("--resultoutput"),
    JUNIT_REPORT_PREFIX("--junitReportPrefix"),
    REPORT_FORMAT("--reportFormat"),
    DOWNLOAD_OUTPUT("--downloadoutput"),
    SCREENSHOT_OUTPUT("--screenshotoutput"),
    TEMPLATE_OUTPUT("--templateoutput"),
    ASPECT("--aspectFile"),
    ENVIRONMENT_PROPERTIES("--env"),
    ENVIRONMENT_PROPERTIES_PREFIX("--env."),
    LOCALE("--locale"),
    LOCALE_CONF("--locale.conf");

    private final String key;

    CommandLineArgument(String aKey) {
        this.key = aKey;
    }

    public String key() {
        return this.key;
    }

    public String createArgument(String aValue) {
        return this.key + "=" + aValue;
    }
}