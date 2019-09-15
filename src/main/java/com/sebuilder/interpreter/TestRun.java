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

import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nonnull;
import java.io.File;

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
    private final TestRunListener listener;
    private final Scenario scenario;
    private final Aspect aspect;
    private final boolean preventContextAspect;
    private TestData vars;
    private TestRunStatus testRunStatus;

    public TestRun(
            TestRunBuilder testRunBuilder,
            Logger log,
            RemoteWebDriver driver,
            TestData initialVars,
            TestRunListener seInterpreterTestListener
    ) {
        this.testRunName = testRunBuilder.getTestRunName(initialVars);
        this.testCase = testRunBuilder.getTestCase();
        this.log = log;
        this.driver = driver;
        this.listener = seInterpreterTestListener;
        this.vars = initialVars.builder()
                .add("_browser", Context.getBrowser())
                .add("_baseDir", Context.getBaseDirectory().getAbsolutePath())
                .add("_dataSourceDir", Context.getDataSourceDirectory().getAbsolutePath())
                .add("_resultDir", seInterpreterTestListener.getResultDir().getAbsolutePath())
                .add("_screenShotDir", seInterpreterTestListener.getScreenShotOutputDirectory().getAbsolutePath())
                .add("_downloadDir", seInterpreterTestListener.getDownloadDirectory().getAbsolutePath())
                .add("_templateDir", seInterpreterTestListener.getTemplateOutputDirectory().getAbsolutePath())
                .build();
        if (this.testCase.relativePath() != null) {
            this.vars = this.vars.add("_relativePath", this.testCase.relativePath().getAbsolutePath());
        }
        this.scenario = testRunBuilder.getScenario();
        this.aspect = this.scenario.aspect();
        this.preventContextAspect = testRunBuilder.isPreventContextAspect();
        this.testRunStatus = TestRunStatus.of(this.scenario, this.testCase);
    }

    public File getRelativePath() {
        return this.testCase.relativePath();
    }

    public void putVars(@Nonnull String key, String value) {
        this.vars = this.vars.add(key, value);
    }

    public String getTestRunName() {
        return this.testRunName;
    }

    public RemoteWebDriver driver() {
        return this.driver;
    }

    public Logger log() {
        return this.log;
    }

    public TestRunListener getListener() {
        return this.listener;
    }

    public TestData vars() {
        return this.vars;
    }

    public int currentStepIndex() {
        return this.testRunStatus.stepIndex();
    }

    public Step currentStep() {
        return this.testCase.steps().get(currentStepIndex());
    }

    public boolean containsKey(String key) {
        return this.currentStep().containsParam(key);
    }

    public String text() {
        return this.string("text");
    }

    public String bindRuntimeVariables(String value) {
        return this.vars.bind(value);
    }

    public boolean getBoolean(@Nonnull String key) {
        return this.containsKey(key) && Boolean.valueOf(this.string(key));
    }

    public String string(@Nonnull String key) {
        String s = this.currentStep().getParam(key);
        if (s == null) {
            throw new RuntimeException("Missing parameter \"" + key + "\" at step #" + currentStepIndex() + ".");
        }
        return this.bindRuntimeVariables(s);
    }

    public boolean hasLocator() {
        return this.currentStep().locatorContains("locator");
    }

    public Locator locator() {
        return this.locator("locator");
    }

    public Locator locator(@Nonnull String key) {
        Locator l = new Locator(this.currentStep().getLocator(key));
        // This kind of variable substitution makes for short code, but it's inefficient.
        l.value = this.bindRuntimeVariables(l.value);
        return l;
    }

    public boolean isStopped() {
        return this.testRunStatus.isStopped();
    }

    public void stop() {
        this.testRunStatus = this.testRunStatus.stop();
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

    public boolean next() {
        try {
            if (!this.runTest()) {
                return this.processTestFailure();
            }
            return this.processTestSuccess();
        } catch (Throwable e) {
            return this.processTestError(e);
        }
    }

    public boolean runTest() {
        this.toNextStepIndex();
        this.startTest();
        return this.currentStep().run(this);
    }

    public void startTest() {
        this.getAdvice().invokeBefore(this);
        this.getListener().startTest(this.currentStepToString());
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
        this.getAdvice().invokeAfter(this);
        return true;
    }

    public boolean processTestFailure() {
        this.getListener().addFailure(currentStepToString() + " failed.");
        this.getAdvice().invokeFailure(this);
        // If a verify failed, we just note this but continue.
        if (this.currentStep().getType().isContinueFailure()) {
            return false;
        }
        // In all other cases, we throw an exception to stop the run.
        throw new AssertionError(currentStepToString() + " failed.");
    }

    public boolean processTestError(Throwable e) {
        this.getListener().addError(e);
        this.getAdvice().invokeFailure(this);
        throw new AssertionError(currentStepToString() + " failed.", e);
    }

    public String currentStepToString() {
        return this.bindRuntimeVariables(this.currentStep().toPrettyString());
    }

    protected boolean start() {
        this.testRunStatus = this.testRunStatus.start();
        return this.getListener().openTestSuite(this.testCase, this.testRunName, this.vars);
    }

    protected boolean hasNext() {
        boolean hasNext = this.stepRest();
        if (!hasNext && this.driver != null) {
            this.quit();
        }
        return hasNext;
    }

    protected boolean stepRest() {
        return testRunStatus.isNeedRunning(this.testCase.steps().size() - 1);
    }

    protected boolean end(boolean success) {
        this.getListener().closeTestSuite();
        if (success && this.testRunStatus.isNeedChain()) {
            return this.chainRun(this.scenario.getChainTo(this.testCase), this.vars);
        }
        return success;
    }

    protected boolean absent(Throwable e) {
        this.getListener().closeTestSuite();
        // If the testCase terminates, the driver will be closed automatically.
        this.quit();
        throw new AssertionError(e);
    }

    protected Aspect.Advice getAdvice() {
        Aspect weaver = this.aspect;
        if (!this.preventContextAspect) {
            weaver = this.aspect.builder()
                    .add(Context.getAspect())
                    .build();
        }
        return weaver.advice(this.currentStep());
    }

    protected boolean chainRun(TestCase chainTo, TestData varTakeOver) {
        this.testRunStatus = this.testRunStatus.chainCalled();
        if (chainTo.skipRunning(varTakeOver)) {
            return this.nextChain(chainTo, varTakeOver);
        }
        if (chainTo.isBreakNestedChain() && !varTakeOver.isLastRow()) {
            return true;
        }
        TestData takeOver = varTakeOver;
        for (TestData data : chainTo.loadData(varTakeOver)) {
            TestData chainData = varTakeOver.clearRowNumber().add(data);
            if (chainTo.isNestedChain()) {
                chainData = chainData.lastRow(data.isLastRow());
            }
            TestRun testRun = createChainRun(chainTo, chainData);
            takeOver = testRun.vars;
            if (!testRun.finish()) {
                return false;
            }
        }
        takeOver = takeOver.lastRow(varTakeOver.isLastRow());
        return chainTo.isNestedChain() || this.nextChain(chainTo, takeOver);
    }

    protected TestRun createChainRun(TestCase chainTo, TestData data) {
        Scenario chainScenario = this.scenario;
        if (!chainTo.isNestedChain()) {
            chainScenario = new Scenario(chainTo).addAspect(this.aspect);
        }
        return new TestRunBuilder(chainTo, chainScenario)
                .addTestRunNamePrefix(this.testRunName + "_")
                .createTestRun(data, this);
    }

    protected boolean nextChain(TestCase chainTo, TestData varTakeOver) {
        if (this.scenario.hasChain(chainTo)) {
            return this.chainRun(this.scenario.getChainTo(chainTo), varTakeOver);
        }
        return true;
    }

    protected void quit() {
        if (this.testCase.closeDriver()) {
            this.log.debug("Quitting driver.");
            try {
                this.driver.quit();
            } catch (Exception e2) {
                //
            }
        }
    }
}
