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
 * A single finish of a test head.
 *
 * @author zarkonnen
 */
public class TestRun implements WebDriverWrapper {
    private final String testRunName;
    private final TestCase testCase;
    private final RemoteWebDriver driver;
    private final Logger log;
    private final TestRunListener listener;
    private final Aspect aspect;
    private final boolean preventContextAspect;
    private InputData vars;
    private TestRunStatus testRunStatus;
    private ChainRunner chainRunner;
    private boolean closeDriver;

    public TestRun(
            String testRunName,
            TestRunBuilder testRunBuilder,
            Logger log,
            RemoteWebDriver driver,
            InputData initialVars,
            TestRunListener seInterpreterTestListener
    ) {
        this.testRunName = testRunName;
        this.testCase = testRunBuilder.getTestCase();
        this.log = log;
        this.driver = driver;
        this.listener = seInterpreterTestListener;
        this.vars = initialVars.builder().add(Context.settings())
                .add("_resultDir", seInterpreterTestListener.getResultDir().getAbsolutePath())
                .add("_screenShotDir", seInterpreterTestListener.getScreenShotOutputDirectory().getAbsolutePath())
                .add("_downloadDir", seInterpreterTestListener.getDownloadDirectory().getAbsolutePath())
                .add("_templateDir", seInterpreterTestListener.getTemplateOutputDirectory().getAbsolutePath())
                .build();
        if (this.testCase.relativePath() != null) {
            this.vars = this.vars.add("_relativePath", this.testCase.relativePath().getAbsolutePath());
        }
        this.aspect = this.testCase.aspect();
        this.preventContextAspect = testRunBuilder.isPreventContextAspect();
        this.testRunStatus = TestRunStatus.of(this.testCase);
    }

    public File getRelativePath() {
        return this.testCase.relativePath();
    }

    public void putVars(@Nonnull String key, String value) {
        this.vars = this.vars.add(key, value);
    }

    public void removeVars(String key) {
        this.vars = this.vars.remove(key);
    }

    public String getTestRunName() {
        return this.testRunName;
    }

    @Override
    public RemoteWebDriver driver() {
        return this.driver;
    }

    public Logger log() {
        return this.log;
    }

    public TestRunListener getListener() {
        return this.listener;
    }

    public InputData vars() {
        return this.vars;
    }

    public Aspect getAspect() {
        return this.aspect;
    }

    public int currentStepIndex() {
        return this.testRunStatus.stepIndex();
    }

    public String formatStepIndex() {
        return String.format("%0" + String.valueOf(this.testCase.steps().size()).length() + "d", currentStepIndex());
    }

    public Step currentStep() {
        return this.testCase.steps().get(currentStepIndex());
    }

    public boolean containsKey(String key) {
        return this.currentStep().containsParam(key);
    }

    public String bindRuntimeVariables(String value) {
        return this.vars.evaluateString(value);
    }

    public boolean getBoolean(@Nonnull String key) {
        return this.containsKey(key) && this.vars().evaluate(this.currentStep().getParam(key));
    }

    public String text() {
        return this.string("text");
    }

    public String string(@Nonnull String key) {
        String s = this.currentStep().getParam(key);
        if (s == null) {
            throw new RuntimeException("Missing parameter \"" + key + "\" at step #" + currentStepIndex() + ".");
        }
        return this.bindRuntimeVariables(s);
    }

    public boolean hasLocator() {
        return this.hasLocator("locator");
    }

    public boolean hasLocator(String key) {
        return this.currentStep().locatorContains(key);
    }

    public Locator locator() {
        return this.locator("locator");
    }

    public Locator locator(@Nonnull String key) {
        Locator l = this.currentStep().getLocator(key);
        return new Locator(this.bindRuntimeVariables(l.type()), this.bindRuntimeVariables(l.value()));
    }

    public boolean isStopped() {
        return this.testRunStatus.isStopped();
    }

    public void stop() {
        this.testRunStatus = this.testRunStatus.stop();
        if (this.chainRunner != null) {
            this.chainRunner.stopRunning();
        }
    }

    public boolean isCloseDriver() {
        return this.closeDriver;
    }

    /**
     * Runs the entire (rest of the) head.
     *
     * @return True if the head ran successfully, false if a verification failed.
     * Any other failure throws an exception.
     * @throws RuntimeException if the head failed.
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
        return this.runTest().success();
    }

    public Step.Result runTest() {
        this.toNextStepIndex();
        return this.currentStep().execute(this);
    }

    public void skipTest() {
        this.toNextStepIndex();
        this.getListener().startTest(
                this.bindRuntimeVariables(
                        this.currentStep()
                                .builder()
                                .put("skip", "true")
                                .build()
                                .toPrettyString()
                )
        );
        this.processTestSuccess(false);
    }

    public boolean startTest() {
        boolean aspectSuccess = this.getAdvice().invokeBefore(this);
        this.getListener().startTest(this.currentStepToString());
        return aspectSuccess;
    }

    public void toNextStepIndex() {
        this.forwardStepIndex(1);
        this.putVars("_stepIndex", String.valueOf(this.currentStepIndex()));
    }

    public void backStepIndex(int count) {
        this.forwardStepIndex(count * -1);
    }

    public void forwardStepIndex(int count) {
        this.getListener().skipTestIndex(count);
        this.testRunStatus = this.testRunStatus.forwardStepIndex(count);
    }

    public boolean processTestSuccess(boolean isAcceptAdvice) {
        this.getListener().endTest();
        if (!isAcceptAdvice) {
            return true;
        }
        return this.getAdvice().invokeAfter(this);
    }

    public boolean processTestFailure(boolean isAcceptAdvice) {
        if (!this.currentStep().type().isContinueAtFailure()) {
            throw new AssertionError(currentStepToString() + " failed.");
        }
        this.getListener().addFailure(currentStepToString() + " failed.");
        if (!isAcceptAdvice) {
            this.getAdvice().invokeFailure(this);
        }
        return false;
    }

    public AssertionError processTestError(Throwable e) {
        this.getListener().addError(e);
        this.getAdvice().invokeFailure(this);
        return new AssertionError(currentStepToString() + " failed.", e);
    }

    public String currentStepToString() {
        return this.bindRuntimeVariables(this.currentStep().toPrettyString());
    }

    protected boolean start() {
        this.testRunStatus = this.testRunStatus.start();
        return this.getListener().openTestSuite(this.testCase, this.testRunName, this.vars);
    }

    protected boolean hasNext() {
        return testRunStatus.isNeedRunning(this.testCase.steps().size() - 1);
    }

    protected boolean end(boolean success) {
        this.getListener().closeTestSuite();
        if (this.testRunStatus.isNeedChain()) {
            return this.chainRun() && success;
        }
        this.quit();
        return success;
    }

    protected boolean absent(Throwable e) {
        this.getListener().closeTestSuite();
        // If the head terminates, the driver will be closed automatically.
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
        return weaver.advice(this.currentStep(), this.vars());
    }

    protected boolean chainRun() {
        this.testRunStatus = this.testRunStatus.chainCalled();
        this.chainRunner = this.createChainRunner();
        return this.chainRunner.finish();
    }

    protected ChainRunner createChainRunner() {
        return new ChainRunner(this);
    }

    protected void quit() {
        if (!this.testCase.shareState()) {
            this.log.debug("Quitting driver.");
            try {
                this.driver.quit();
                this.closeDriver = true;
            } catch (Exception e2) {
                //
            }
        }
    }

    protected static class ChainRunner implements TestRunner {
        private final TestRun parent;
        private final TestCaseChains chains;
        private TestRun lastRun;
        private InputData lastRunVar;
        private int chainIndex;

        public ChainRunner(TestRun parent) {
            this.parent = parent;
            this.chains = parent.testCase.chains();
        }

        public boolean finish() {
            InputData chainInitialVar = this.parent.vars();
            this.chainIndex = 0;
            boolean success = true;
            for (TestCase nextChain : this.chains) {
                final InputData chainVar = chainInitialVar;
                success = nextChain.map(it -> it.addAspect(this.parent.getAspect()).setShareInput(chainVar))
                        .run(this, this.parent.getListener()) && success;
                if (this.chains.isTakeOverLastRun() && this.lastRunVar != null) {
                    chainInitialVar = this.lastRunVar;
                }
                this.chainIndex++;
            }
            return success;
        }

        @Override
        public STATUS execute(TestRunBuilder testRunBuilder, InputData data, TestRunListener testRunListener) {
            if (this.isStopped()) {
                return STATUS.STOPPED;
            }
            this.lastRun = testRunBuilder.createTestRun(data, this.parent, this.chainIndex);
            boolean result = this.lastRun.finish();
            if (this.lastRun.isStopped()) {
                return STATUS.STOPPED;
            } else if (!result) {
                return STATUS.FAILED;
            }
            if (this.chains.isTakeOverLastRun() && data.isLastRow()) {
                this.lastRunVar = lastRun.vars();
            }
            return STATUS.SUCCESS;
        }

        protected boolean isStopped() {
            return this.parent.isStopped();
        }

        protected void stopRunning() {
            if (this.lastRun != null) {
                lastRun.stop();
            }
        }
    }
}
