package com.sebuilder.interpreter.javafx.event.script;

public class StepDeleteEvent {

    private final int stepIndex;

    public StepDeleteEvent(int stepIndex) {
        this.stepIndex = stepIndex;
    }

    public int getStepIndex() {
        return stepIndex;
    }
}
