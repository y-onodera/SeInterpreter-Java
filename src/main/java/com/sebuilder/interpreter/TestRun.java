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

import com.google.common.collect.Maps;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.util.HashMap;
import java.util.Map;

/**
 * A single run of a test script.
 *
 * @author zarkonnen
 */
public class TestRun {
    private final String testRunName;
    private final Script script;
    private final HashMap<String, String> vars = new HashMap<>();
    private final RemoteWebDriver driver;
    private final Logger log;
    private final SeInterpreterTestListener listener;
    private final ScriptChain scriptChain;
    private int stepIndex = -1;
    private boolean chainRun;
    private boolean finished;
    private boolean stop;

    public TestRun(
            String testRunName,
            Script script,
            Logger log,
            RemoteWebDriver driver,
            Map<String, String> initialVars,
            SeInterpreterTestListener seInterpreterTestListener,
            ScriptChain scriptChain
    ) {
        this.testRunName = testRunName;
        this.script = script;
        this.log = log;
        this.driver = driver;
        if (initialVars != null) {
            vars.putAll(initialVars);
        }
        this.listener = seInterpreterTestListener;
        this.vars.put("_browser", Context.getInstance().getBrowser());
        this.vars.put("_baseDir", Context.getInstance().getBaseDirectory().getAbsolutePath());
        this.vars.put("_dataSourceDir", Context.getInstance().getDataSourceDirectory().getAbsolutePath());
        this.vars.put("_resultDir", seInterpreterTestListener.getResultDir().getAbsolutePath());
        this.vars.put("_screenShotDir", seInterpreterTestListener.getScreenShotOutputDirectory().getAbsolutePath());
        this.vars.put("_templateDir", seInterpreterTestListener.getTemplateOutputDirectory().getAbsolutePath());
        this.vars.put("_downloadDir", seInterpreterTestListener.getDownloadDirectory().getAbsolutePath());
        this.scriptChain = scriptChain;
        this.chainRun = this.scriptChain.containsKey(this.script);
    }

    public String getTestRunName() {
        return this.testRunName;
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
        return this.script.steps().get(this.stepIndex);
    }

    /**
     * @param key
     * @return
     */
    public boolean containsKey(String key) {
        return this.currentStep().containsParam(key);
    }

    /**
     * @return
     */
    public String text() {
        return this.string("text");
    }

    public boolean getBoolean(String filterTag) {
        return containsKey(filterTag) && Boolean.valueOf(string(filterTag));
    }

    /**
     * Fetches a String parameter from the current step.
     *
     * @param paramName The parameter's name.
     * @return The parameter's value.
     */
    public String string(String paramName) {
        String s = this.currentStep().getParam(paramName);
        if (s == null) {
            throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" + (this.stepIndex + 1) + ".");
        }
        return TestRuns.replaceVariable(s, this.vars);
    }

    public boolean hasLocator() {
        return this.currentStep().locatorContains("locator");
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
        Locator l = this.currentStep().getLocator(paramName);
        if (l == null) {
            throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" + (this.stepIndex + 1) + ".");
        }
        // This kind of variable substitution makes for short code, but it's inefficient.
        l.value = TestRuns.replaceVars(l.value, this.vars);
        return l;
    }

    public boolean isFinished() {
        return this.finished;
    }

    public boolean isStopped() {
        return this.stop;
    }

    /**
     * Runs the entire (rest of the) script.
     *
     * @return True if the script ran successfully, false if a verification failed.
     * Any other failure throws an exception.
     * @throws RuntimeException if the script failed.
     */
    public boolean finish() {
        try {
            this.start();
            boolean success = true;
            try {
                while (this.hasNext()) {
                    success = this.next() && success;
                }
            } catch (Throwable e) {
                return this.absent(e);
            }
            return this.end(success);
        } finally {
            this.finished = true;
        }
    }

    public void start() {
        this.finished = false;
        this.stop = false;
        this.getListener().openTestSuite(this.script, this.testRunName, this.vars);
    }

    public void stop() {
        this.stop = true;
    }

    /**
     * @return True if there is another step to execute.
     */
    public boolean hasNext() {
        boolean hasNext = this.stepRest();
        if (!hasNext && this.driver != null) {
            this.quit();
        }
        return hasNext;
    }

    public boolean stepRest() {
        return !this.isStopped() && this.stepIndex < this.script.steps().size() - 1;
    }

    /**
     * Executes the next step.
     *
     * @return True on success.
     */
    public boolean next() {
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

    public boolean runTest() {
        this.toNextStepIndex();
        this.startTest();
        return this.currentStep().run(this);
    }

    public void startTest() {
        this.getListener().startTest(currentStep().getName() != null ? this.currentStep().getName() : TestRuns.replaceVariable(this.currentStep().toPrettyString(), this.vars));
    }

    public void toNextStepIndex() {
        this.forwardStepIndex(1);
    }

    public void backStepIndex(int count) {
        this.forwardStepIndex(count * -1);
    }

    public void forwardStepIndex(int count) {
        this.getListener().skipTestIndex(count);
        this.stepIndex = this.stepIndex + count;
    }

    public boolean processTestSuccess() {
        this.getListener().endTest();
        return true;
    }

    public boolean processTestFailure() {
        this.getListener().addFailure(this.currentStep() + " failed.");
        // If a verify failed, we just note this but continue.
        if (this.currentStep().getType() instanceof Verify) {
            return false;
        }
        // In all other cases, we throw an exception to stop the run.
        throw new AssertionError(this.currentStep() + " failed.");
    }

    public boolean processTestError(Throwable e) {
        this.getListener().addError(e);
        throw new AssertionError(this.currentStep() + " failed.", e);
    }

    public boolean end(boolean success) {
        this.getListener().closeTestSuite();
        if (this.chainRun && !this.isStopped()) {
            this.chainRun = false;
            return this.chainRun(this.scriptChain.get(this.script));
        }
        return success;
    }

    public boolean absent(Throwable e) {
        this.getListener().closeTestSuite();
        // If the script terminates, the driver will be closed automatically.
        this.quit();
        throw new AssertionError(e);
    }

    public void quit() {
        if (this.script.closeDriver()) {
            this.log.debug("Quitting driver.");
            try {
                this.driver.quit();
            } catch (Exception e2) {
                //
            }
        }
    }

    private boolean chainRun(Script chainTo) {
        if (chainTo.skipRunning(this.vars)) {
            if (this.scriptChain.containsKey(chainTo)) {
                return this.chainRun(this.scriptChain.get(chainTo));
            }
            return true;
        }
        boolean success = true;
        for (Map<String, String> data : chainTo.loadData(this.vars)) {
            Map<String, String> chainData = Maps.newHashMap(this.vars);
            chainData.remove(DataSource.ROW_NUMBER);
            chainData.putAll(data);
            TestRun testRun = createChainRun(chainTo, chainData);
            if (!testRun.finish()) {
                return false;
            }
        }
        return success;
    }

    private TestRun createChainRun(Script chainTo, Map<String, String> data) {
        return new TestRunBuilder(chainTo)
                .addChain(this.scriptChain)
                .addTestRunNamePrefix(this.testRunName + "_")
                .createTestRun(this.log
                        , data
                        , this
                        , this.listener);
    }

}
