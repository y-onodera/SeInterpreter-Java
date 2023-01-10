package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Interceptor;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.util.Objects;
import java.util.concurrent.Exchanger;

public class Debugger implements Interceptor {

    private final Exchanger<String> exchanger;

    private final BooleanProperty disable = new SimpleBooleanProperty();

    private String lastOperation;

    public Debugger() {
        this.exchanger = new Exchanger<>();
    }

    public BooleanProperty disableProperty() {
        return this.disable;
    }

    @Override
    public boolean isPointcut(final Step step, final InputData vars) {
        return true;
    }

    @Override
    public boolean invokeBefore(final TestRun testRun) {
        try {
            if (!Objects.equals(this.lastOperation, "stop")) {
                this.disable.set(false);
                this.lastOperation = this.exchanger.exchange("await");
                this.disable.set(true);
            }
            return true;
        } catch (final InterruptedException e) {
            return false;
        }
    }

    public void stepOver() throws InterruptedException {
        this.exchanger.exchange("stepOver");
    }

    public void stop() throws InterruptedException {
        this.exchanger.exchange("stop");
    }
}
