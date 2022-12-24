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

    public TestRunBuilder(TestCase testCase) {
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
        return testCase;
    }

    public boolean isPreventContextAspect() {
        return this.testCase.preventContextAspect();
    }

    public TestRunBuilder setShareInput(InputData shareInput) {
        this.shareInput = this.shareInput.add(shareInput);
        return this;
    }

    public TestRunBuilder addTestRunNameSuffix(String aTestRunNameSuffix) {
        if (this.testRunNameSuffix == null) {
            this.testRunNameSuffix = "";
        }
        if (aTestRunNameSuffix != null) {
            this.testRunNameSuffix = this.testRunNameSuffix + aTestRunNameSuffix;
        }
        return this;
    }

    public TestRunBuilder addTestRunNamePrefix(String aTestRunNamePrefix) {
        if (this.testRunNamePrefix == null) {
            this.testRunNamePrefix = "";
        }
        if (aTestRunNamePrefix != null) {
            this.testRunNamePrefix = this.testRunNamePrefix + aTestRunNamePrefix;
        }
        return this;
    }

    public TestRun createTestRun(Logger log, WebDriverFactory webDriverFactory, Map<String, String> webDriverConfig, Long implicitWaitTime, Long pageLoadWaitTime, InputData initialVars, TestRun previousRun, TestRunListener seInterpreterTestListener) {
        final RemoteWebDriver driver;
        if (this.testCase.shareState() && previousRun != null && previousRun.driver() != null) {
            driver = previousRun.driver();
        } else {
            driver = createDriver(log, webDriverFactory, webDriverConfig);
        }
        if (implicitWaitTime != null && implicitWaitTime > 0) {
            driver.manage().timeouts().implicitlyWait(Duration.of(implicitWaitTime, ChronoUnit.SECONDS));
        }
        if (pageLoadWaitTime != null && pageLoadWaitTime > 0) {
            driver.manage().timeouts().pageLoadTimeout(Duration.of(pageLoadWaitTime, ChronoUnit.SECONDS));
        }
        return this.createTestRun(log, driver, initialVars, seInterpreterTestListener);
    }

    public TestRun createTestRun(InputData initialVars, TestRun previousRun, int index) {
        return this.createTestRun(
                previousRun.getTestRunName() + "_" + index + "_" + this.testCase.name()
                , previousRun.log()
                , previousRun.driver()
                , initialVars
                , previousRun.getListener());
    }

    public TestRun createTestRun(Logger log, RemoteWebDriver driver, InputData initialVars, TestRunListener seInterpreterTestListener) {
        return this.createTestRun(this.testCase.name(), log, driver, initialVars, seInterpreterTestListener);
    }

    public TestRunBuilder copy() {
        return new TestRunBuilder(this.testCase)
                .setShareInput(this.shareInput)
                .addTestRunNamePrefix(this.testRunNamePrefix)
                .addTestRunNameSuffix(this.testRunNameSuffix);
    }

    protected TestRun createTestRun(String testRunName, Logger log, RemoteWebDriver driver, InputData initialVars, TestRunListener seInterpreterTestListener) {
        InputData data = this.shareInput.clearRowNumber().add(initialVars).lastRow(initialVars.isLastRow());
        return new TestRun(this.getTestRunName(testRunName, data), this, log, driver, data, seInterpreterTestListener);
    }

    protected String getTestRunName(String testRunName, InputData initialVars) {
        String result = testRunName;
        if (isTestCaseAlreadySaved() && result.contains(".")) {
            String suffix = "";
            Matcher m = DUPLICATE_PATTERN.matcher(result);
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
        return Strings.isNullOrEmpty(testCase.path());
    }

    private RemoteWebDriver createDriver(Logger log, WebDriverFactory webDriverFactory, Map<String, String> webDriverConfig) {
        log.debug("Initialising driver.");
        try {
            return webDriverFactory.make(webDriverConfig);
        } catch (Exception e) {
            throw new RuntimeException("Test finish failed: unable to create driver.", e);
        }
    }
}
