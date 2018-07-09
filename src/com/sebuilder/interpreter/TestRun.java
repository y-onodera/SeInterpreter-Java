/*
 * Copyright 2012 Sauce Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebuilder.interpreter;

import com.sebuilder.interpreter.webdriverfactory.WebDriverFactory;
import org.apache.commons.logging.Log;
import org.openqa.selenium.Keys;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * A single run of a test script.
 *
 * @author zarkonnen
 */
public class TestRun {
    private Script script;
    private HashMap<String, String> vars = new HashMap<>();
    private int stepIndex = -1;
    private RemoteWebDriver driver;
    private Log log;
    private WebDriverFactory webDriverFactory = SeInterpreter.DEFAULT_DRIVER_FACTORY;
    private HashMap<String, String> webDriverConfig = new HashMap<>();
    private Long implicitlyWaitDriverTimeout;
    private Long pageLoadDriverTimeout;
    private SeInterpreterTestListener listener;

    public TestRun(
            Script script,
            Log log,
            WebDriverFactory webDriverFactory,
            HashMap<String, String> webDriverConfig,
            Long implicitlyWaitDriverTimeout,
            Long pageLoadDriverTimeout,
            Map<String, String> initialVars, SeInterpreterTestListener seInterpreterTestListener) {
        this(script, log, null, webDriverFactory, webDriverConfig, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars, seInterpreterTestListener);
    }

    public TestRun(
            Script script,
            Log log,
            RemoteWebDriver driver,
            Long implicitlyWaitDriverTimeout,
            Long pageLoadDriverTimeout,
            Map<String, String> initialVars, SeInterpreterTestListener seInterpreterTestListener) {
        this(script, log, driver, null, null, implicitlyWaitDriverTimeout, pageLoadDriverTimeout, initialVars, seInterpreterTestListener);
    }

    public TestRun(
            Script script,
            Log log,
            RemoteWebDriver driver,
            WebDriverFactory webDriverFactory,
            HashMap<String, String> webDriverConfig,
            Long implicitlyWaitDriverTimeout,
            Long pageLoadDriverTimeout,
            Map<String, String> initialVars,
            SeInterpreterTestListener seInterpreterTestListener) {
        this.script = script;
        this.log = log;
        this.driver = driver;
        this.webDriverFactory = webDriverFactory;
        this.webDriverConfig = webDriverConfig;
        if (initialVars != null) {
            vars.putAll(initialVars);
        }
        this.listener = seInterpreterTestListener;
        setTimeouts(implicitlyWaitDriverTimeout, pageLoadDriverTimeout);
        vars.put("_resultDir", Context.getInstance().getResultOutputDirectory().getAbsolutePath());
        vars.put("_dataSourceDir", Context.getInstance().getDataSourceDirectory().getAbsolutePath());
        vars.put("_screenShotDir", Context.getInstance().getScreenShotOutputDirectory().getAbsolutePath());
        vars.put("_templateDir", Context.getInstance().getTemplateOutputDirectory().getAbsolutePath());
        vars.put("_downloadDir", Context.getInstance().getDownloadDirectory().getAbsolutePath());
        vars.put("_suiteName", this.suiteName());
        vars.put("_scriptName", this.scriptName());
    }

    /**
     * @param script
     * @return
     */
    public TestRun createTestRun(Script script) {
        return new TestRun(script
                , this.log
                , this.driver
                , null
                , null
                , null
                , null
                , null
                , this.listener
        );
    }

    public String scriptName() {
        if (script.path == null) {
            return script.name;
        }
        return script.name.substring(0, script.name.indexOf(".")) + vars.get(DataSource.ROW_NUMBER);
    }

    public String suiteName() {
        return this.listener.suiteName();
    }

    public String testName() {
        return this.listener.testName();
    }

    /**
     * @return The driver instance being used.
     */
    public RemoteWebDriver driver() {
        return driver;
    }

    /**
     * @return The logger being used.
     */
    public Log log() {
        return log;
    }

    /**
     * @return The HashMap of variables.
     */
    public HashMap<String, String> vars() {
        return vars;
    }

    /**
     * @return The step that is being/has just been executed.
     */
    public Step currentStep() {
        return script.steps.get(stepIndex);
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        return this.currentStep().stringParams.containsKey(key);
    }

    /**
     * @return
     */
    public String text() {
        return this.string("text");
    }

    /**
     * Fetches a String parameter from the current step.
     *
     * @param paramName The parameter's name.
     * @return The parameter's value.
     */
    public String string(String paramName) {
        String s = currentStep().stringParams.get(paramName);
        if (s == null) {
            throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" +
                    (stepIndex + 1) + ".");
        }
        // Sub special keys using the !{keyname} syntax.
        for (Keys k : Keys.values()) {
            s = s.replace("!{" + k.name() + "}", k.toString());
        }
        // This kind of variable substitution makes for short code, but it's inefficient.
        for (Map.Entry<String, String> v : vars.entrySet()) {
            s = s.replace("${" + v.getKey() + "}", v.getValue());
        }
        return s;
    }

    /**
     * @return
     */
    public Locator locator() {
        return this.locator("locator");
    }

    /**
     * Fetches a Locator parameter from the current step.
     *
     * @param paramName The parameter's name.
     * @return The parameter's value.
     */
    public Locator locator(String paramName) {
        Locator l = new Locator(currentStep().locatorParams.get(paramName));
        if (l == null) {
            throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" +
                    (stepIndex + 1) + ".");
        }
        // This kind of variable substitution makes for short code, but it's inefficient.
        for (Map.Entry<String, String> v : vars.entrySet()) {
            l.value = l.value.replace("${" + v.getKey() + "}", v.getValue());
        }
        return l;
    }

    /**
     * Runs the entire (rest of the) script.
     *
     * @return True if the script ran successfully, false if a verification failed.
     * Any other failure throws an exception.
     * @throws RuntimeException if the script failed.
     */
    public boolean finish() {
        boolean success = true;
        try {
            while (hasNext()) {
                success = next() && success;
            }
        } catch (RuntimeException e) {
            // If the script terminates, the driver will be closed automatically.
            if (script.closeDriver) {
                try {
                    driver.quit();
                } catch (Exception e2) {
                }
                driver = null;
            }
            throw e;
        }
        return success;
    }


    /**
     * @return True if there is another step to execute.
     */
    public boolean hasNext() {
        boolean hasNext = stepIndex < script.steps.size() - 1;
        if (!hasNext && driver != null && script.closeDriver) {
            log.debug("Quitting driver.");
            driver.quit();
            driver = null;
        }
        return hasNext;
    }

    /**
     * Executes the next step.
     *
     * @return True on success.
     */
    public boolean next() {
        initRemoteWebDriver();
        boolean result;
        try {
            result = runTest();
        } catch (Throwable e) {
            return processTestError(e);
        }
        if (!result) {
            return processTestFailure();
        }
        return processTestSuccess();
    }

    /**
     *
     */
    public void forwardStepIndex(int count) {
        stepIndex = stepIndex + count;
    }

    /**
     *
     */
    public void backStepIndex(int count) {
        stepIndex = stepIndex - count;
    }

    /**
     *
     */
    public void toNextStepIndex() {
        this.forwardStepIndex(1);
    }

    private boolean processTestSuccess() {
        listener.endTest();
        return true;
    }

    private boolean processTestFailure() {
        listener.addFailure(currentStep() + " failed.");
        // If a verify failed, we just note this but continue.
        if (currentStep().type instanceof Verify) {
            return false;
        }
        // In all other cases, we throw an exception to stop the run.
        throw new AssertionError(currentStep() + " failed.");
    }

    private boolean processTestError(Throwable e) {
        listener.addError(e);
        throw new AssertionError(currentStep() + " failed.", e);
    }

    private boolean runTest() {
        this.toNextStepIndex();
        listener.startTest(currentStep().name != null ? currentStep().name : currentStep().toPrettyString());
        return currentStep().type.run(this);
    }

    /**
     * Initialises remoteWebDriver by invoking factory and set timeouts when
     * needed
     */
    private void initRemoteWebDriver() {
        if (driver == null) {
            log.debug("Initialising driver.");
            try {
                driver = webDriverFactory.make(webDriverConfig);
                if (implicitlyWaitDriverTimeout != null) {
                    driver.manage().timeouts().implicitlyWait(implicitlyWaitDriverTimeout, TimeUnit.SECONDS);
                }
                if (pageLoadDriverTimeout != null) {
                    driver.manage().timeouts().pageLoadTimeout(pageLoadDriverTimeout, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                throw new RuntimeException("Test run failed: unable to create driver.", e);
            }
        }
    }

    /**
     * @param implicitlyWaitDriverTimeout
     * @param pageLoadDriverTimeout
     */
    private void setTimeouts(Long implicitlyWaitDriverTimeout, Long pageLoadDriverTimeout) {
        if (implicitlyWaitDriverTimeout != null && implicitlyWaitDriverTimeout > 0) {
            this.implicitlyWaitDriverTimeout = Long.valueOf(implicitlyWaitDriverTimeout);
        }
        if (pageLoadDriverTimeout != null && pageLoadDriverTimeout > 0) {
            this.pageLoadDriverTimeout = Long.valueOf(pageLoadDriverTimeout);
        }
    }

}
