package com.sebuilder.interpreter.javafx.event.script;

public class StepLoadEvent {
    private final int stepIndex;

    public StepLoadEvent(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public int getStepIndex() {
        return stepIndex;
    }
}
