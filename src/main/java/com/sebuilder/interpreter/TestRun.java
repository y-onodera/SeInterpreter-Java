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

import com.sebuilder.interpreter.step.Verify;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * A single run of a test testCase.
 *
 * @author zarkonnen
 */
public class TestRun {
    private final String testRunName;
    private final TestCase testCase;
    private final RemoteWebDriver driver;
    private final Logger log;
    private final SeInterpreterTestListener listener;
    private final Scenario scenario;
    private TestRunStatus testRunStatus;
    private TestData vars;

    public TestRun(
            String testRunName,
            TestCase testCase,
            Logger log,
            RemoteWebDriver driver,
            TestData initialVars,
            SeInterpreterTestListener seInterpreterTestListener,
            Scenario scenario
    ) {
        this.testRunName = testRunName;
        this.testCase = testCase;
        this.log = log;
        this.driver = driver;
        this.listener = seInterpreterTestListener;
        this.vars = initialVars.builder()
                .add("_browser", Context.getInstance().getBrowser())
                .add("_baseDir", Context.getInstance().getBaseDirectory().getAbsolutePath())
                .add("_dataSourceDir", Context.getInstance().getDataSourceDirectory().getAbsolutePath())
                .add("_resultDir", seInterpreterTestListener.getResultDir().getAbsolutePath())
                .add("_screenShotDir", seInterpreterTestListener.getScreenShotOutputDirectory().getAbsolutePath())
                .add("_downloadDir", seInterpreterTestListener.getDownloadDirectory().getAbsolutePath())
                .add("_templateDir", seInterpreterTestListener.getTemplateOutputDirectory().getAbsolutePath())
                .build();
        this.scenario = scenario;
        this.testRunStatus = TestRunStatus.of(this.scenario, this.testCase);
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
    public TestData vars() {
        return this.vars;
    }

    /**
     * @return The step that is being/has just been executed.
     */
    public Step currentStep() {
        return this.testCase.steps().get(this.testRunStatus.stepIndex());
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
            throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" + this.testRunStatus.stepIndex() + ".");
        }
        return this.vars.bind(s);
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
            throw new RuntimeException("Missing parameter \"" + paramName + "\" at step #" + this.testRunStatus.stepIndex() + ".");
        }
        // This kind of variable substitution makes for short code, but it's inefficient.
        l.value = this.vars.bind(l.value);
        return l;
    }

    public boolean isStopped() {
        return this.testRunStatus.isStopped();
    }

    public void putVars(String index, String s) {
        this.vars = this.vars.add(index, s);
    }

    /**
     * Runs the entire (rest of the) testCase.
     *
     * @return True if the testCase ran successfully, false if a verification failed.
     * Any other failure throws an exception.
     * @throws RuntimeException if the testCase failed.
     */
    public boolean finish() {
        try {
            boolean success = this.start();
            try {
                while (this.hasNext()) {
                    success = this.next() && success;
                }
            } catch (Throwable e) {
                return this.absent(e);
            }
            return this.end(success);
        } finally {
            this.testRunStatus = this.testRunStatus.finish();
        }
    }

    public boolean start() {
        this.testRunStatus = this.testRunStatus.start();
        this.getListener().openTestSuite(this.testCase, this.testRunName, this.vars);
        return true;
    }

    public void stop() {
        this.testRunStatus = this.testRunStatus.stop();
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
        return testRunStatus.isNeedRunning(this.testCase.steps().size() - 1);
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
        this.getListener().startTest(currentStep().getName() != null ? this.currentStep().getName() : this.vars.bind(this.currentStep().toPrettyString()));
    }

    public void toNextStepIndex() {
        this.forwardStepIndex(1);
    }

    public void backStepIndex(int count) {
        this.forwardStepIndex(count * -1);
    }

    public void forwardStepIndex(int count) {
        this.getListener().skipTestIndex(count);
        this.testRunStatus = this.testRunStatus.forwardStepIndex(count);
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
        if (this.testRunStatus.isNeedChain()) {
            return this.chainRun(this.scenario.getChainTo(this.testCase));
        }
        return success;
    }

    public boolean absent(Throwable e) {
        this.getListener().closeTestSuite();
        // If the testCase terminates, the driver will be closed automatically.
        this.quit();
        throw new AssertionError(e);
    }

    public void quit() {
        if (this.testCase.closeDriver()) {
            this.log.debug("Quitting driver.");
            try {
                this.driver.quit();
            } catch (Exception e2) {
                //
            }
        }
    }

    private boolean chainRun(TestCase chainTo) {
        this.testRunStatus = this.testRunStatus.chainCalled();
        if (chainTo.skipRunning(this.vars)) {
            if (this.scenario.hasChain(chainTo)) {
                return this.chainRun(this.scenario.getChainTo(chainTo));
            }
            return true;
        }
        boolean success = true;
        for (TestData data : chainTo.loadData(this.vars)) {
            TestData chainData = this.vars.clearRowNumber().add(data);
            TestRun testRun = createChainRun(chainTo, chainData);
            if (!testRun.finish()) {
                return false;
            }
        }
        return success;
    }

    private TestRun createChainRun(TestCase chainTo, TestData data) {
        return new TestRunBuilder(chainTo, this.scenario)
                .addTestRunNamePrefix(this.testRunName + "_")
                .createTestRun(this.log
                        , data
                        , this
                        , this.listener);
    }

}
