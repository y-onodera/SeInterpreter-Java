package com.sebuilder.interpreter.application;

public enum CommandLineArgument {

    IMPLICITLY_WAIT("--implicitlyWait"),
    PAGE_LOAD_TIMEOUT("--pageLoadTimeout"),
    STEP_TYPE_PACKAGE("--stepTypePackage"),
    STEP_TYPE_PACKAGE2("--stepTypePackage2"),
    DRIVER("--driver"),
    DRIVER_PATH("--driverPath"),
    DRIVER_CONFIG_PREFIX("--driver."),
    DATASOURCE_PACKAGE("--datasourcePackage"),
    DATASOURCE_ENCODING("--datasource.encoding"),
    DATASOURCE_DIRECTORY("--datasource.directory"),
    SCREENSHOT_OUTPUT("--screenshotoutput"),
    TEMPLATE_OUTPUT("--templateoutput"),
    RESULT_OUTPUT("--resultoutput"),
    DOWNLOAD_OUTPUT("--downloadoutput"),
    ASPECT("--aspectFile"),
    ENVIRONMENT_PROPERTIES("--env"),
    ENVIRONMENT_PROPERTIES_PREFIX("--env."),
    LOCALE("--locale"),
    LOCALE_CONF("--locale.conf");

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
