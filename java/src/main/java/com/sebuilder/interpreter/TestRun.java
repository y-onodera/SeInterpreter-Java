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
import java.util.function.Predicate;

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
    private final Pointcut includeFilter;
    private final Pointcut excludeFilter;
    private final boolean preventContextAspect;
    private InputData vars;
    private TestRunStatus testRunStatus;
    private ChainRunner chainRunner;
    private boolean closeDriver;

    public TestRun(
            final String testRunName,
            final TestRunBuilder testRunBuilder,
            final Logger log,
            final RemoteWebDriver driver,
            final InputData initialVars,
            final TestRunListener seInterpreterTestListener
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
        this.includeFilter = this.testCase.includeTestRun();
        this.excludeFilter = this.testCase.excludeTestRun();
        this.preventContextAspect = testRunBuilder.isPreventContextAspect();
        this.testRunStatus = TestRunStatus.of(this.testCase);
    }

    public File getRelativePath() {
        return this.testCase.relativePath();
    }

    public void putVars(@Nonnull final String key, final String value) {
        this.vars = this.vars.add(key, value);
    }

    public void removeVars(final String key) {
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

    public Aspect getAspect(final Predicate<Interceptor> condition) {
        return this.aspect.filter(condition);
    }

    public int currentStepIndex() {
        return this.testRunStatus.stepIndex();
    }

    public String formatStepIndex() {
        return String.format("%0" + String.valueOf(this.testCase.steps().size()).length() + "d", this.currentStepIndex());
    }

    public Step currentStep() {
        return this.testCase.steps().get(this.currentStepIndex());
    }

    public boolean containsKey(final String key) {
        return this.currentStep().containsParam(key);
    }

    public String bindRuntimeVariables(final String value) {
        return this.vars.evaluateString(value);
    }

    public boolean getBoolean(@Nonnull final String key) {
        return this.containsKey(key) && this.vars().evaluate(this.currentStep().getParam(key));
    }

    public String text() {
        return this.string("text");
    }

    public String string(@Nonnull final String key) {
        final String s = this.currentStep().getParam(key);
        if (s == null) {
            throw new RuntimeException("Missing parameter \"" + key + "\" at step #" + this.currentStepIndex() + ".");
        }
        return this.bindRuntimeVariables(s);
    }

    public boolean hasLocator() {
        return this.hasLocator("locator");
    }

    public boolean hasLocator(final String key) {
        return this.currentStep().locatorContains(key);
    }

    public Locator locator() {
        return this.locator("locator");
    }

    public Locator locator(@Nonnull final String key) {
        final Locator l = this.currentStep().getLocator(key);
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
            } catch (final Throwable e) {
                return this.absent(e);
            }
            return this.end(success);
        } finally {
            this.testRunStatus = this.testRunStatus.finish();
        }
    }

    public boolean next() {
        return this.runStep().success();
    }

    public Step.Result runStep() {
        this.toNextStepIndex();
        if (this.includeFilter.isHandle(this.currentStep(), this.vars)
                && !this.excludeFilter.isHandle(this.currentStep(), this.vars)) {
            return this.currentStep().execute(this);
        }
        return new Step.Result(true, 0);
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
        final boolean aspectSuccess = this.getAdvice().invokeBefore(this);
        this.getListener().startTest(this.currentStepToString());
        return aspectSuccess;
    }

    public void toNextStepIndex() {
        this.forwardStepIndex(1);
        this.vars = this.vars.stepIndex(this.currentStepIndex());
    }

    public void backStepIndex(final int count) {
        this.forwardStepIndex(count * -1);
    }

    public void forwardStepIndex(final int count) {
        this.testRunStatus = this.testRunStatus.forwardStepIndex(count);
        this.getListener().setStepIndex(this.currentStepIndex());
    }

    public boolean processTestSuccess(final boolean isAcceptAdvice) {
        this.getListener().endTest();
        if (!isAcceptAdvice) {
            return true;
        }
        return this.getAdvice().invokeAfter(this);
    }

    public boolean processTestFailure(final boolean isAcceptAdvice) {
        if (!this.currentStep().type().isContinueAtFailure()) {
            throw new AssertionError(this.currentStepToString() + " failed.");
        }
        this.getListener().addFailure(this.currentStepToString() + " failed.");
        if (!isAcceptAdvice) {
            this.getAdvice().invokeFailure(this);
        }
        return false;
    }

    public AssertionError processTestError(final Throwable e) {
        this.getListener().addError(e);
        this.getAdvice().invokeFailure(this);
        return new AssertionError(this.currentStepToString() + " failed.", e);
    }

    public String currentStepToString() {
        return this.bindRuntimeVariables(this.currentStep().toPrettyString());
    }

    protected boolean start() {
        this.testRunStatus = this.testRunStatus.start();
        return this.getListener().openTestSuite(this.testCase, this.testRunName, this.vars);
    }

    protected boolean hasNext() {
        return this.testRunStatus.isNeedRunning(this.testCase.steps().size() - 1);
    }

    protected boolean end(final boolean success) {
        this.getListener().closeTestSuite();
        if (this.testRunStatus.isNeedChain()) {
            return this.chainRun() && success;
        }
        this.quit();
        return success;
    }

    protected boolean absent(final Throwable e) {
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
        return this.chainRunner.run();
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
            } catch (final Exception e2) {
                //
            }
        }
    }

    protected static class ChainRunner implements TestRunner {
        private final TestRun parent;
        private final TestCaseChains chains;
        private final boolean takeOverLastRun;
        private TestRun lastRun;
        private InputData lastRunVar;
        private int chainIndex;

        public ChainRunner(final TestRun parent) {
            this.parent = parent;
            this.chains = parent.testCase.chains();
            this.takeOverLastRun = this.chains.isTakeOverLastRun();
        }

        public boolean run() {
            this.lastRunVar = this.parent.vars();
            this.chainIndex = 0;
            boolean success = true;
            for (final TestCase nextChain : this.chains) {
                success = nextChain.map(it -> it.addAspect(this.parent.getAspect(Interceptor::isTargetingChain))
                                .setShareInput(this.lastRunVar))
                        .run(this, this.parent.getListener()) && success;
                this.chainIndex++;
            }
            return success;
        }

        @Override
        public STATUS execute(final TestRunBuilder testRunBuilder, final InputData data, final TestRunListener testRunListener) {
            if (this.isStopped()) {
                return STATUS.STOPPED;
            }
            this.lastRun = testRunBuilder.createTestRun(data, this.parent, this.chainIndex);
            final boolean result = this.lastRun.finish();
            if (this.lastRun.isStopped()) {
                return STATUS.STOPPED;
            } else if (!result) {
                return STATUS.FAILED;
            }
            if (this.takeOverLastRun && data.isLastRow()) {
                this.lastRunVar = this.lastRun.vars();
            }
            return STATUS.SUCCESS;
        }

        protected boolean isStopped() {
            return this.parent.isStopped();
        }

        protected void stopRunning() {
            if (this.lastRun != null) {
                this.lastRun.stop();
            }
        }
    }
}
