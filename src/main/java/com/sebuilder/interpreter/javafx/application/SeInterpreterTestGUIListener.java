package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.SeInterpreterTestListener;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.Result;
import com.sebuilder.interpreter.javafx.event.replay.StepResultSetEvent;
import org.apache.logging.log4j.Logger;

public class SeInterpreterTestGUIListener extends SeInterpreterTestListener {

    public SeInterpreterTestGUIListener(Logger aLog) {
        super(aLog);
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
