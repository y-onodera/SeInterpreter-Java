package com.sebuilder.interpreter;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestRunBuilder {

    private static final Pattern DUPLICATE_PATTERN = Pattern.compile(".+\\.[^\\.]+(\\(\\d+\\)$)");
    private final Script script;
    private ScriptChain scriptChain;
    private final Map<String, String> shareInput;
    private String testRunNamePrefix;
    private String testRunNameSuffix;

    public TestRunBuilder(Script script) {
        this.script = script;
        this.scriptChain = new ScriptChain();
        this.shareInput = Maps.newHashMap();
    }

    public String getScriptName() {
        return this.getScriptFileName().substring(0, this.script.name().indexOf('.'));
    }

    public String getScriptFileName() {
        return this.script.name();
    }

    public boolean closeDriver() {
        return this.script.closeDriver();
    }

    public TestRunBuilder addChain(ScriptChain scriptChain) {
        Script scriptFrom = this.script;
        while (scriptChain.containsKey(scriptFrom)) {
            Script chainTo = scriptChain.get(scriptFrom);
            this.scriptChain = this.scriptChain.add(scriptFrom, chainTo);
            scriptFrom = chainTo;
        }
        return this;
    }

    public TestRunBuilder setShareInput(Map<String, String> shareInput) {
        this.shareInput.putAll(shareInput);
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

    public List<Map<String, String>> loadData() {
        return this.script.loadData(this.shareInput);
    }

    public TestRun createTestRun(Logger log, WebDriverFactory webDriverFactory, HashMap<String, String> webDriverConfig, Long implicityWaitTime, Long pageLoadWaitTime, Map<String, String> initialVars, TestRun previousRun, SeInterpreterTestListener seInterpreterTestListener) {
        final RemoteWebDriver driver;
        if (this.script.usePreviousDriverAndVars() && previousRun != null && previousRun.driver() != null) {
            driver = previousRun.driver();
        } else {
            driver = createDriver(log, webDriverFactory, webDriverConfig);
        }
        if (implicityWaitTime != null && implicityWaitTime.longValue() > 0) {
            driver.manage().timeouts().implicitlyWait(implicityWaitTime, TimeUnit.SECONDS);
        }
        if (pageLoadWaitTime != null && pageLoadWaitTime.longValue() > 0) {
            driver.manage().timeouts().pageLoadTimeout(pageLoadWaitTime, TimeUnit.SECONDS);
        }
        return this.createTestRun(log, driver, initialVars, seInterpreterTestListener);
    }

    public TestRun createTestRun(Logger log, Map<String, String> initialVars, TestRun previousRun, SeInterpreterTestListener seInterpreterTestListener) {
        return this.createTestRun(log, previousRun.driver(), initialVars, seInterpreterTestListener);
    }

    public TestRun createTestRun(Logger log, RemoteWebDriver driver, Map<String, String> initialVars, SeInterpreterTestListener seInterpreterTestListener) {
        final Script copy = this.script.copy();
        final ScriptChain copyScriptChain = this.scriptChain.replace(this.script, copy);
        Map<String, String> data = Maps.newHashMap(this.shareInput);
        data.putAll(initialVars);
        return new TestRun(getTestRunName(data), copy, log, driver, data, seInterpreterTestListener, copyScriptChain);
    }

    public String getTestRunName(Map<String, String> initialVars) {
        String result = this.script.name();
        if (script.path() != null && result.contains(".")) {
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
        if (initialVars.containsKey(DataSource.ROW_NUMBER)) {
            result = result + "_row_" + initialVars.get(DataSource.ROW_NUMBER);
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
