package com.sebuilder.interpreter.javafx.model;

import com.sebuilder.interpreter.TestCaseBuilder;
import com.sebuilder.interpreter.TestRun;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.concurrent.Exchanger;
import java.util.function.Function;

public class Debugger {

    private final Exchanger<String> exchanger;

    private final ObjectProperty<STATUS> debugStatus;

    public Debugger() {
        this.exchanger = new Exchanger<>();
        this.debugStatus = new SimpleObjectProperty<>();
        this.reset();
    }

    public ObjectProperty<STATUS> debugStatusProperty() {
        return this.debugStatus;
    }

    public STATUS getDebugStatus() {
        return this.debugStatus.get();
    }

    public boolean await(final TestRun testRun) {
        try {
            this.debugStatus.set(STATUS.await);
            final STATUS lastOperation = STATUS.valueOf(this.exchanger.exchange(this.debugStatus.get().name()));
            this.debugStatus.set(lastOperation);
            return true;
        } catch (final InterruptedException e) {
            return false;
        }
    }

    public void stepOver() throws InterruptedException {
        this.exchanger.exchange(STATUS.stepOver.name());
    }

    public void stop() throws InterruptedException {
        this.exchanger.exchange(STATUS.stop.name());
    }

    public void resume() throws InterruptedException {
        this.exchanger.exchange(STATUS.resume.name());
    }

    public void pause() {
        this.debugStatus.set(STATUS.pause);
    }

    public Debugger reset() {
        this.debugStatus.set(STATUS.resume);
        return this;
    }

    public Function<TestCaseBuilder, TestCaseBuilder> append() {
        return it -> BreakPoint.isSetting().test(it.getAspect()) ? it : it.insertAspect(new BreakPoint(new HashMap<>(), this).toAspect());
    }

    public enum STATUS {
        await, stepOver, stop, pause, resume
    }
}
