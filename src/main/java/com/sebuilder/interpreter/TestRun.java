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
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.annotation.Nonnull;
import java.io.File;
import java.util.Objects;

/**
 * A single finish of a test head.
 *
 * @author zarkonnen
 */
public class TestRun {
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
        this.aspect = this.testCase.getAspect();
        this.preventContextAspect = testRunBuilder.isPreventContextAspect();
        this.testRunStatus = TestRunStatus.of(this.testCase);
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

    public InputData vars() {
        return this.vars;
    }

    public Aspect getAspect() {
        return this.aspect;
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
        return this.containsKey(key) && Boolean.parseBoolean(this.string(key));
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

    public int getWindowHeight() {
        return ((Number) this.driver().executeScript("return window.innerHeight;", new Object[0])).intValue();
    }

    public int getWindowWidth() {
        return ((Number)driver().executeScript("return window.innerWidth;", new Object[0])).intValue();
    }

    public int getClientHeight() {
        return ((Number) this.driver().executeScript("return document.documentElement.clientHeight;", new Object[0])).intValue();
    }

    public int getClientWidth() {
        return ((Number)driver().executeScript("return document.documentElement.clientWidth;", new Object[0])).intValue();
    }

    public int getContentHeight() {
        WebElement body = driver().findElementByTagName("body");
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-y"), "hidden")) {
            return getClientHeight();
        }
        return ((Number) driver().executeScript("return Math.max(document.body.scrollHeight, document.body.offsetHeight,document.documentElement.clientHeight, document.documentElement.scrollHeight, document.documentElement.offsetHeight);", new Object[0])).intValue();
    }

    public int getContentWidth() {
        WebElement body = driver().findElementByTagName("body");
        if (Objects.equals(body.getCssValue("overflow"), "hidden") || Objects.equals(body.getCssValue("overflow-x"), "hidden")) {
            return getClientWidth();
        }
        return ((Number) driver().executeScript("return Math.max(document.body.scrollWidth, document.body.offsetWidth,document.documentElement.clientWidth, document.documentElement.scrollWidth, document.documentElement.offsetWidth);", new Object[0])).intValue();
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
        if (this.currentStep().getType().isContinueAtFailure()) {
            return false;
        }
        // In all other cases, we throw an exception to stop the finish.
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
        return this.stepRest();
    }

    protected boolean stepRest() {
        return testRunStatus.isNeedRunning(this.testCase.steps().size() - 1);
    }

    protected boolean end(boolean success) {
        this.getListener().closeTestSuite();
        if (success && this.testRunStatus.isNeedChain()) {
            return this.chainRun();
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
        return weaver.advice(this.currentStep());
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
        if (!this.testCase.isShareState()) {
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
            this.chains = parent.testCase.getChains();
        }

        public boolean finish() {
            InputData chainInitialVar = this.parent.vars();
            this.chainIndex = 0;
            for (TestCase nextChain : this.chains) {
                final InputData chainVar = chainInitialVar;
                if (!nextChain.map(it -> it.addAspect(this.parent.getAspect()).setShareInput(chainVar))
                        .run(this, this.parent.getListener())) {
                    return false;
                }
                if (this.chains.isTakeOverLastRun() && this.lastRunVar != null) {
                    chainInitialVar = this.lastRunVar;
                }
                this.chainIndex++;
            }
            return true;
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
