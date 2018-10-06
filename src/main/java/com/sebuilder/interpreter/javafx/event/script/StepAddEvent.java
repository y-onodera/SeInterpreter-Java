package com.sebuilder.interpreter.javafx.event.script;

public class StepAddEvent {

    private final String stepType;

    public StepAddEvent(String stepType) {
        this.stepType = stepType;
    }

    public String getStepType() {
        return stepType;
    }
}
