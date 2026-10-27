package com.sebuilder.interpreter.javafx.model;

import com.sebuilder.interpreter.TestCaseBuilder;
import com.sebuilder.interpreter.TestRun;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.HashMap;
import java.util.concurrent.Exchanger;
import java.util.function.Function;

public class Debugger {

    private final Exchanger<String> exchanger;

    // bound to the GUI, so it is only updated on the FX thread as a copy of status
    private final ObjectProperty<STATUS> debugStatus;

    // referred to by the replay thread, so it is the source of truth
    private volatile STATUS status;

    public Debugger() {
        this.exchanger = new Exchanger<>();
        this.debugStatus = new SimpleObjectProperty<>();
        this.reset();
    }

    public ObjectProperty<STATUS> debugStatusProperty() {
        return this.debugStatus;
    }

    public STATUS getDebugStatus() {
        return this.status;
    }

    public boolean await(final TestRun testRun) {
        try {
            this.setDebugStatus(STATUS.await);
            final STATUS lastOperation = STATUS.valueOf(this.exchanger.exchange(STATUS.await.name()));
            this.setDebugStatus(lastOperation);
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
        this.setDebugStatus(STATUS.pause);
    }

    public Debugger reset() {
        this.setDebugStatus(STATUS.resume);
        return this;
    }

    public Function<TestCaseBuilder, TestCaseBuilder> append() {
        return it -> BreakPoint.isSetting().test(it.getAspect()) ? it : it.insertAspect(new BreakPoint(new HashMap<>(), this).toAspect());
    }

    private void setDebugStatus(final STATUS newStatus) {
        this.status = newStatus;
        if (Platform.isFxApplicationThread()) {
            this.debugStatus.set(newStatus);
        } else {
            // copy the latest status at run time so that delayed updates do not overwrite newer ones
            Platform.runLater(() -> this.debugStatus.set(this.status));
        }
    }

    public enum STATUS {
        await, stepOver, stop, pause, resume
    }
}
