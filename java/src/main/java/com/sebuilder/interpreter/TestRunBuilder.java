package com.sebuilder.interpreter;

import com.google.common.base.Strings;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRunBuilder {

    private static final Pattern DUPLICATE_PATTERN = Pattern.compile(".+\\.[^.]+(\\(\\d+\\)$)");
    private final TestCase testCase;
    private InputData shareInput;
    private String testRunNamePrefix;
    private String testRunNameSuffix;

    public TestRunBuilder(final TestCase testCase) {
        this.testCase = testCase;
        this.shareInput = testCase.shareInput();
    }

    public String getScriptName() {
        return this.getScriptFileName().substring(0, this.testCase.name().indexOf('.'));
    }

    public String getScriptFileName() {
        return this.testCase.name();
    }

    public TestCase getTestCase() {
        return this.testCase;
    }

    public boolean isPreventContextAspect() {
        return this.testCase.preventContextAspect();
    }

    public TestRunBuilder setShareInput(final InputData shareInput) {
        this.shareInput = this.shareInput.add(shareInput);
        return this;
    }

    public TestRunBuilder addTestRunNameSuffix(final String aTestRunNameSuffix) {
        if (this.testRunNameSuffix == null) {
            this.testRunNameSuffix = "";
        }
        if (aTestRunNameSuffix != null) {
            this.testRunNameSuffix = this.testRunNameSuffix + aTestRunNameSuffix;
        }
        return this;
    }

    public TestRunBuilder addTestRunNamePrefix(final String aTestRunNamePrefix) {
        if (this.testRunNamePrefix == null) {
            this.testRunNamePrefix = "";
        }
        if (aTestRunNamePrefix != null) {
            this.testRunNamePrefix = this.testRunNamePrefix + aTestRunNamePrefix;
        }
        return this;
    }

    public TestRun createTestRun(final Logger log, final WebDriverFactory webDriverFactory, final Map<String, String> webDriverConfig, final Long implicitWaitTime, final Long pageLoadWaitTime, final InputData initialVars, final TestRun previousRun, final TestRunListener seInterpreterTestListener) {
        final RemoteWebDriver driver;
        if (this.testCase.shareState() && previousRun != null && previousRun.driver() != null) {
            driver = previousRun.driver();
        } else {
            driver = this.createDriver(log, webDriverFactory, webDriverConfig);
        }
        if (implicitWaitTime != null && implicitWaitTime > 0) {
            driver.manage().timeouts().implicitlyWait(Duration.of(implicitWaitTime, ChronoUnit.SECONDS));
        }
        if (pageLoadWaitTime != null && pageLoadWaitTime > 0) {
            driver.manage().timeouts().pageLoadTimeout(Duration.of(pageLoadWaitTime, ChronoUnit.SECONDS));
        }
        return this.createTestRun(log, driver, initialVars, seInterpreterTestListener);
    }

    public TestRun createTestRun(final InputData initialVars, final TestRun previousRun, final int index) {
        return this.createTestRun(
                previousRun.getTestRunName() + "_" + index + "_" + this.testCase.name()
                , previousRun.log()
                , previousRun.driver()
                , initialVars
                , previousRun.getListener());
    }

    public TestRun createTestRun(final Logger log, final RemoteWebDriver driver, final InputData initialVars, final TestRunListener seInterpreterTestListener) {
        return this.createTestRun(this.testCase.name(), log, driver, initialVars, seInterpreterTestListener);
    }

    public TestRunBuilder copy() {
        return new TestRunBuilder(this.testCase)
                .setShareInput(this.shareInput)
                .addTestRunNamePrefix(this.testRunNamePrefix)
                .addTestRunNameSuffix(this.testRunNameSuffix);
    }

    protected TestRun createTestRun(final String testRunName, final Logger log, final RemoteWebDriver driver, final InputData initialVars, final TestRunListener seInterpreterTestListener) {
        final InputData data = this.shareInput.clearRowNumber().add(initialVars).lastRow(initialVars.isLastRow());
        return new TestRun(this.getTestRunName(testRunName, data), this, log, driver, data, seInterpreterTestListener);
    }

    protected String getTestRunName(final String testRunName, final InputData initialVars) {
        String result = testRunName;
        if (this.isTestCaseAlreadySaved() && result.contains(".")) {
            String suffix = "";
            final Matcher m = DUPLICATE_PATTERN.matcher(result);
            if (m.matches()) {
                suffix = m.group(1);
            }
            result = result.substring(0, result.lastIndexOf(".")) + suffix;
        }
        if (this.testRunNameSuffix != null) {
            result = result + this.testRunNameSuffix;
        }
        if (this.testRunNamePrefix != null) {
            result = this.testRunNamePrefix + result;
        }
        if (initialVars.rowNumber() != null) {
            result = result + "_row_" + initialVars.rowNumber();
        }
        return result;
    }

    protected boolean isTestCaseAlreadySaved() {
        return !Strings.isNullOrEmpty(this.testCase.path());
    }

    private RemoteWebDriver createDriver(final Logger log, final WebDriverFactory webDriverFactory, final Map<String, String> webDriverConfig) {
        log.debug("Initialising driver.");
        try {
            return webDriverFactory.make(webDriverConfig);
        } catch (final Exception e) {
            throw new RuntimeException("Test finish failed: unable to create driver.", e);
        }
    }
}
