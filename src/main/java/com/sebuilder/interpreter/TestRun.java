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
import org.apache.logging.log4j.Logger;
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
    private RemoteWebDriver driver;
    private Logger log;
    private WebDriverFactory webDriverFactory = SeInterpreter.DEFAULT_DRIVER_FACTORY;
    private HashMap<String, String> webDriverConfig = new HashMap<>();
    private Long implicitlyWaitDriverTimeout;
    private Long pageLoadDriverTimeout;
    private SeInterpreterTestListener listener;
    private Map<Script, Script> scriptChain = new HashMap<>();
    private int stepIndex = -1;
    private boolean chainRun;

    public TestRun(
            Script script,
            Logger log,
            WebDriverFactory webDriverFactory,
            HashMap<String, String> webDriverConfig,
            Long implicitlyWaitDriverTimeout,
            Long pageLoadDriverTimeout,
            Map<String, String> initialVars, SeInterpreterTestListener seInterpreterTestListener,
            Map<Script, Script> scriptChain) {
        this(script
                , log
                , null
                , webDriverFactory
                , webDriverConfig
                , implicitlyWaitDriverTimeout
                , pageLoadDriverTimeout
                , initialVars
                , seInterpreterTestListener
                , scriptChain);
    }

    public TestRun(
            Script script,
            Logger log,
            RemoteWebDriver driver,
            Long implicitlyWaitDriverTimeout,
            Long pageLoadDriverTimeout,
            Map<String, String> initialVars, SeInterpreterTestListener seInterpreterTestListener,
            Map<Script, Script> scriptChain) {
        this(script
                , log
                , driver
                , null
                , null
                , implicitlyWaitDriverTimeout
                , pageLoadDriverTimeout
                , initialVars
                , seInterpreterTestListener
                , scriptChain);
    }

    public TestRun(
            Script script,
            Logger log,
            RemoteWebDriver driver,
            WebDriverFactory webDriverFactory,
            HashMap<String, String> webDriverConfig,
            Long implicitlyWaitDriverTimeout,
            Long pageLoadDriverTimeout,
            Map<String, String> initialVars,
            SeInterpreterTestListener seInterpreterTestListener,
            Map<Script, Script> scriptChain
    ) {
        this.script = script;
        this.log = log;
        this.driver = driver;
        this.webDriverFactory = webDriverFactory;
        this.webDriverConfig = webDriverConfig;
        if (initialVars != null) {
            vars.putAll(initialVars);
        }
        this.listener = seInterpreterTestListener;
        this.setTimeouts(implicitlyWaitDriverTimeout, pageLoadDriverTimeout);
        this.vars.put("_resultDir", Context.getInstance().getResultOutputDirectory().getAbsolutePath());
        this.vars.put("_dataSourceDir", Context.getInstance().getDataSourceDirectory().getAbsolutePath());
        this.vars.put("_screenShotDir", Context.getInstance().getScreenShotOutputDirectory().getAbsolutePath());
        this.vars.put("_templateDir", Context.getInstance().getTemplateOutputDirectory().getAbsolutePath());
        this.vars.put("_downloadDir", Context.getInstance().getDownloadDirectory().getAbsolutePath());
        this.vars.put("_suiteName", this.suiteName());
        this.vars.put("_scriptName", this.scriptName());
        this.scriptChain.putAll(scriptChain);
        this.chainRun = this.scriptChain.containsKey(this.script);
    }

    public String scriptName() {
        if (this.script.path == null) {
            return this.script.name;
        }
        return this.script.name.substring(0, this.script.name.indexOf(".")) + this.vars.get(DataSource.ROW_NUMBER);
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
        return this.driver;
    }

    /**
     * @return The logger being used.
     */
    public Logger log() {
        return this.log;
    }

    /**
     * @return The listener being used.
     */
    public SeInterpreterTestListener getListener() {
        return this.listener;
    }

    /**
     * @return The HashMap of variables.
     */
    public HashMap<String, String> vars() {
        return this.vars;
    }

    /**
     * @return The step that is being/has just been executed.
     */
    public Step currentStep() {
        return this.script.steps.get(this.stepIndex);
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
        String s = this.currentStep().stringParams.get(paramName);
        if (s == null) {
            throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" + (this.stepIndex + 1) + ".");
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
        Locator l = this.currentStep().locatorParams.get(paramName);
        if (l == null) {
            throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" + (this.stepIndex + 1) + ".");
        }
        // This kind of variable substitution makes for short code, but it's inefficient.
        for (Map.Entry<String, String> v : this.vars.entrySet()) {
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
            while (this.hasNext()) {
                success = this.next() && success;
            }
        } catch (RuntimeException e) {
            // If the script terminates, the driver will be closed automatically.
            this.quit();
            throw e;
        }
        return success;
    }

    /**
     * @return True if there is another step to execute.
     */
    public boolean hasNext() {
        boolean hasNext = this.stepRest() || this.chainRun;
        if (!hasNext && this.driver != null) {
            this.quit();
        }
        return hasNext;
    }

    /**
     * Executes the next step.
     *
     * @return True on success.
     */
    public boolean next() {
        if (!this.stepRest() && this.chainRun) {
            this.chainRun = false;
            return this.chainRun(this.scriptChain.get(this.script));
        }
        this.initRemoteWebDriver();
        boolean result;
        try {
            result = this.runTest();
        } catch (Throwable e) {
            return this.processTestError(e);
        }
        if (!result) {
            return this.processTestFailure();
        }
        return this.processTestSuccess();
    }

    /**
     *
     */
    public void forwardStepIndex(int count) {
        this.getListener().skipTestIndex(count);
        this.stepIndex = this.stepIndex + count;
    }

    /**
     *
     */
    public void backStepIndex(int count) {
        this.forwardStepIndex(count * -1);
    }

    /**
     *
     */
    public void toNextStepIndex() {
        this.forwardStepIndex(1);
    }

    public boolean runTest() {
        this.toNextStepIndex();
        this.startTest();
        return this.currentStep().type.run(this);
    }

    public void startTest() {
        this.getListener().startTest(currentStep().name != null ? this.currentStep().name : this.currentStep().toPrettyString());
    }

    public boolean processTestSuccess() {
        this.getListener().endTest();
        return true;
    }

    public boolean processTestFailure() {
        this.getListener().addFailure(this.currentStep() + " failed.");
        // If a verify failed, we just note this but continue.
        if (this.currentStep().type instanceof Verify) {
            return false;
        }
        // In all other cases, we throw an exception to stop the run.
        throw new AssertionError(this.currentStep() + " failed.");
    }

    public boolean processTestError(Throwable e) {
        this.getListener().addError(e);
        throw new AssertionError(this.currentStep() + " failed.", e);
    }


    /**
     * Initialises remoteWebDriver by invoking factory and set timeouts when
     * needed
     */
    private void initRemoteWebDriver() {
        if (this.driver == null) {
            this.log.debug("Initialising driver.");
            try {
                this.driver = this.webDriverFactory.make(this.webDriverConfig);
                if (this.implicitlyWaitDriverTimeout != null) {
                    this.driver.manage().timeouts().implicitlyWait(this.implicitlyWaitDriverTimeout, TimeUnit.SECONDS);
                }
                if (this.pageLoadDriverTimeout != null) {
                    this.driver.manage().timeouts().pageLoadTimeout(this.pageLoadDriverTimeout, TimeUnit.SECONDS);
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
            this.implicitlyWaitDriverTimeout = implicitlyWaitDriverTimeout;
        }
        if (pageLoadDriverTimeout != null && pageLoadDriverTimeout > 0) {
            this.pageLoadDriverTimeout = pageLoadDriverTimeout;
        }
    }

    private void quit() {
        if (this.script.closeDriver()) {
            this.log.debug("Quitting driver.");
            try {
                this.driver.quit();
            } catch (Exception e2) {
                //
            }
            this.driver = null;
        }
    }

    private boolean stepRest() {
        return this.stepIndex < this.script.steps.size() - 1;
    }

    private TestRun createChainRun(Script chainTo, Map<String, String> data) {
        return new TestRun(chainTo
                , this.log
                , this.driver
                , this.webDriverFactory
                , this.webDriverConfig
                , this.implicitlyWaitDriverTimeout
                , this.pageLoadDriverTimeout
                , data
                , this.listener
                , this.scriptChain);
    }

    private boolean chainRun(Script chainTo) {
        boolean success = true;
        for (Map<String, String> data : chainTo.loadData()) {
            TestRun testRun = createChainRun(chainTo, data);
            if (!testRun.finish()) {
                return false;
            }
        }
        return success;
    }

}
