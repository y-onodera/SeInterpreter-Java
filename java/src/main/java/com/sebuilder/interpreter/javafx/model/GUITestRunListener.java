package com.sebuilder.interpreter.javafx.model;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestRunListener;
import com.sebuilder.interpreter.TestRunListenerWrapper;

public class GUITestRunListener extends TestRunListenerWrapper {
    private final SeInterpreter application;

    public GUITestRunListener(final TestRunListener delegate, final SeInterpreter application) {
        super(delegate);
        this.application = application;
    }

    @Override
    public boolean openTestSuite(final TestCase testCase, final String testRunName, final InputData aProperty) {
        if (this.application.getSuite().get(testCase.name()) != null) {
            this.application.selectScript(testCase.name());
        }
        return super.openTestSuite(testCase, testRunName, aProperty);
    }

    @Override
    public void setStepIndex(final int count) {
        if (!this.delegate.isAspectRunning()) {
            super.setStepIndex(count);
        }
    }

    @Override
    public void startTest(final String testName) {
        super.startTest(testName);
        if (!this.delegate.isAspectRunning()) {
            this.application.updateReplayStatus(this.getStepIndex(), Result.START);
        }
    }

    @Override
    public void addError(final Throwable throwable) {
        super.addError(throwable);
        if (!this.delegate.isAspectRunning()) {
            this.application.updateReplayStatus(this.getStepIndex(), Result.ERROR);
        }
    }

    @Override
    public void addFailure(final String message) {
        super.addFailure(message);
        if (!this.delegate.isAspectRunning()) {
            this.application.updateReplayStatus(this.getStepIndex(), Result.FAILURE);
        }
    }

    @Override
    public void endTest() {
        super.endTest();
        if (!this.delegate.isAspectRunning()) {
            this.application.updateReplayStatus(this.getStepIndex(), Result.SUCCESS);
        }
    }
}
