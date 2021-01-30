package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.application.TestRunListenerImpl;
import org.apache.logging.log4j.Logger;

public class GUITestRunListener extends TestRunListenerImpl {
    private final SeInterpreterApplication application;

    public GUITestRunListener(Logger aLog, SeInterpreterApplication application) {
        super(aLog);
        this.application = application;
    }

    @Override
    public boolean openTestSuite(TestCase testCase, String testRunName, InputData aProperty) {
        if (this.application.getSuite().get(testCase.name()) != null) {
            this.application.selectScript(testCase.name());
        }
        return super.openTestSuite(testCase, testRunName, aProperty);
    }

    @Override
    public void startTest(String testName) {
        super.startTest(testName);
        this.application.updateReplayStatus(this.getStepNo(), Result.START);
    }

    @Override
    public void addError(Throwable throwable) {
        this.application.updateReplayStatus(this.getStepNo(), Result.ERROR);
        super.addError(throwable);
    }

    @Override
    public void addFailure(String message) {
        this.application.updateReplayStatus(this.getStepNo(), Result.FAILURE);
        super.addFailure(message);
    }

    @Override
    public void endTest() {
        this.application.updateReplayStatus(this.getStepNo(), Result.SUCCESS);
        super.endTest();
    }
}
