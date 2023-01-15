package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Interceptor;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Objects;
import java.util.concurrent.Exchanger;

public class Debugger implements Interceptor {

    private final Exchanger<String> exchanger;

    private final StringProperty debugStatus = new SimpleStringProperty();

    private BreakPoint breakPoint;

    public Debugger(final BreakPoint breakPoint) {
        this.exchanger = new Exchanger<>();
        this.breakPoint = breakPoint;
    }

    public StringProperty debugStatusProperty() {
        return this.debugStatus;
    }

    @Override
    public boolean isPointcut(final Step step, final InputData vars) {
        return this.breakPoint.apply(step, vars);
    }

    @Override
    public boolean invokeBefore(final TestRun testRun) {
        try {
            this.debugStatus.set("await");
            final String lastOperation = this.exchanger.exchange("await");
            this.debugStatus.set(lastOperation);
            if (Objects.equals(lastOperation, "stop")) {
                testRun.stop();
            }
            return true;
        } catch (final InterruptedException e) {
            return false;
        }
    }

    public void stepOver() throws InterruptedException {
        this.breakPoint = BreakPoint.STEP_BY_STEP;
        this.exchanger.exchange("stepOver");
    }

    public void stop() throws InterruptedException {
        this.pause();
        this.exchanger.exchange("stop");
    }

    public void resume() throws InterruptedException {
        if (this.breakPoint == BreakPoint.STEP_BY_STEP) {
            this.breakPoint = BreakPoint.DO_NOT_BREAK;
        }
        this.exchanger.exchange("resume");
    }

    public void pause() {
        this.breakPoint = BreakPoint.STEP_BY_STEP;
    }
}
