package com.sebuilder.interpreter.application;

public enum CommandLineArgument {

    IMPLICITLY_WAIT("--implicitlyWait"),
    PAGE_LOAD_TIMEOUT("--pageLoadTimeout"),
    WAIT_FOR_MAX_MS("--waitFor.maxMs"),
    WAIT_FOR_INTERVAL_MS("--waitFor.intervalMs"),
    DRIVER("--driver"),
    DRIVER_PATH("--driverPath"),
    DRIVER_CONFIG_PREFIX("--driver."),
    DRIVER_CONFIG_BROWSER_VERSION("--driver.browserVersion"),
    DATASOURCE_ENCODING("--datasource.encoding"),
    DATASOURCE_DIRECTORY("--datasource.directory"),
    RESULT_OUTPUT("--resultoutput"),
    REPORT_PREFIX("--reportPrefix"),
    REPORT_FORMAT("--reportFormat"),
    DOWNLOAD_OUTPUT("--downloadoutput"),
    SCREENSHOT_OUTPUT("--screenshotoutput"),
    TEMPLATE_OUTPUT("--templateoutput"),
    EXPECT_SCREENSHOT_DIRECTORY("--expectScreenshotDirectory"),
    ASPECT("--aspectFile"),
    ENVIRONMENT_PROPERTIES("--env"),
    ENVIRONMENT_PROPERTIES_PREFIX("--env."),
    LOCALE("--locale"),
    LOCALE_CONF("--locale.conf");

    private final String key;

    CommandLineArgument(final String aKey) {
        this.key = aKey;
    }

    public String key() {
        return this.key;
    }

    public String createArgument(final String aValue) {
        return this.key + "=" + aValue;
    }
}
