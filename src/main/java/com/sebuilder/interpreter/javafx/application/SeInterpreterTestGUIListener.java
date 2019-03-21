package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.SeInterpreterTestListenerImpl;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestData;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.Result;
import com.sebuilder.interpreter.javafx.event.replay.StepResultSetEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptSelectEvent;
import org.apache.logging.log4j.Logger;

public class SeInterpreterTestGUIListener extends SeInterpreterTestListenerImpl {

    public SeInterpreterTestGUIListener(Logger aLog) {
        super(aLog);
    }

    @Override
    public boolean openTestSuite(TestCase testCase, String testRunName, TestData aProperty) {
        EventBus.publish(new ScriptSelectEvent(testCase.name()));
        return super.openTestSuite(testCase, testRunName, aProperty);
    }

    @Override
    public void startTest(String testName) {
        super.startTest(testName);
        EventBus.publish(new StepResultSetEvent(this.getStepNo(), Result.START));
    }

    @Override
    public void addError(Throwable throwable) {
        EventBus.publish(new StepResultSetEvent(this.getStepNo(), Result.ERROR));
        super.addError(throwable);
    }

    @Override
    public void addFailure(String message) {
        EventBus.publish(new StepResultSetEvent(this.getStepNo(), Result.FAILURE));
        super.addFailure(message);
    }

    @Override
    public void endTest() {
        EventBus.publish(new StepResultSetEvent(this.getStepNo(), Result.SUCCESS));
        super.endTest();
    }
}
