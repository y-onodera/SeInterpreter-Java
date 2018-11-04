package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.SimpleSeInterpreterTestListener;
import com.sebuilder.interpreter.javafx.EventBus;
import com.sebuilder.interpreter.javafx.Result;
import com.sebuilder.interpreter.javafx.event.replay.StepResultSetEvent;
import com.sebuilder.interpreter.javafx.event.script.ScriptSelectEvent;
import javafx.concurrent.Task;
import org.apache.logging.log4j.Logger;

import java.util.Map;

public class SeInterpreterTestGUIListener extends SimpleSeInterpreterTestListener {

    Task task;

    public SeInterpreterTestGUIListener(Logger aLog) {
        super(aLog);
    }

    @Override
    public boolean openTestSuite(Script script, String testRunName, Map<String, String> aProperty) {
        EventBus.publish(new ScriptSelectEvent(script.name));
        return super.openTestSuite(script, testRunName, aProperty);
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
