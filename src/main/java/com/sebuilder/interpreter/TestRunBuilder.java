package com.sebuilder.interpreter;

import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRunBuilder {

    private static final Pattern DUPLICATE_PATTERN = Pattern.compile(".+\\.[^\\.]+(\\(\\d+\\)$)");
    private final TestCase testCase;
    private Scenario scenario;
    private TestData shareInput;
    private boolean preventContextAspect;
    private String testRunNamePrefix;
    private String testRunNameSuffix;

    public TestRunBuilder(TestCase testCase) {
        this(testCase, new Scenario(testCase));
    }

    public TestRunBuilder(TestCase testCase, Scenario aScenario) {
        this.testCase = testCase;
        this.scenario = aScenario;
        this.shareInput = new TestData();
        this.preventContextAspect = false;
    }

    public String getScriptName() {
        return this.getScriptFileName().substring(0, this.testCase.name().indexOf('.'));
    }

    public String getScriptFileName() {
        return this.testCase.name();
    }

    public boolean closeDriver() {
        return this.testCase.closeDriver();
    }

    public TestCase getTestCase() {
        return testCase;
    }

    public Scenario getScenario() {
        return scenario;
    }

    public boolean isPreventContextAspect() {
        return this.preventContextAspect;
    }

    public TestRunBuilder setShareInput(TestData shareInput) {
        this.shareInput = this.shareInput.add(shareInput);
        return this;
    }

    public TestRunBuilder addTestRunNameSuffix(String testRunNameSuffix) {
        if (this.testRunNameSuffix == null) {
            this.testRunNameSuffix = "";
        }
        this.testRunNameSuffix = this.testRunNameSuffix + testRunNameSuffix;
        return this;
    }

    public TestRunBuilder addTestRunNamePrefix(String testRunNamePrefix) {
        if (this.testRunNamePrefix == null) {
            this.testRunNamePrefix = "";
        }
        this.testRunNamePrefix = this.testRunNamePrefix + testRunNamePrefix;
        return this;
    }

    public TestRunBuilder preventContextAspect(boolean aPreventAspect) {
        this.preventContextAspect = aPreventAspect;
        return this;
    }

    public List<TestData> loadData() {
        return this.testCase.loadData(this.shareInput);
    }

    public TestRun createTestRun(Logger log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, Long implicitWaitTime, Long pageLoadWaitTime, TestData initialVars, TestRun previousRun, SeInterpreterTestListener seInterpreterTestListener) {
        final RemoteWebDriver driver;
        if (this.testCase.usePreviousDriverAndVars() && previousRun != null && previousRun.driver() != null) {
            driver = previousRun.driver();
        } else {
            driver = createDriver(log, webDriverFactory, webDriverConfig);
        }
        if (implicitWaitTime != null && implicitWaitTime.longValue() > 0) {
            driver.manage().timeouts().implicitlyWait(implicitWaitTime, TimeUnit.SECONDS);
        }
        if (pageLoadWaitTime != null && pageLoadWaitTime.longValue() > 0) {
            driver.manage().timeouts().pageLoadTimeout(pageLoadWaitTime, TimeUnit.SECONDS);
        }
        return this.createTestRun(log, driver, initialVars, seInterpreterTestListener);
    }

    public TestRun createTestRun(TestData initialVars, TestRun previousRun) {
        return this.createTestRun(previousRun.log(), previousRun.driver(), initialVars, previousRun.getListener());
    }

    public TestRun createTestRun(Logger log, RemoteWebDriver driver, TestData initialVars, SeInterpreterTestListener seInterpreterTestListener) {
        TestData data = this.shareInput.add(initialVars);
        return new TestRun(this, log, driver, data, seInterpreterTestListener);
    }

    public String getTestRunName(TestData initialVars) {
        String result = this.testCase.name();
        if (testCase.path() != null && result.contains(".")) {
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

    private RemoteWebDriver createDriver(Logger log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig) {
        log.debug("Initialising driver.");
        try {
            return webDriverFactory.make(webDriverConfig);
        } catch (Exception e) {
            throw new RuntimeException("Test run failed: unable to create driver.", e);
        }
    }
}
