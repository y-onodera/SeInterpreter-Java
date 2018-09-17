package com.sebuilder.interpreter.javafx.event.script;

public class SelectNewStepEvent {

    private final String stepType;

    public SelectNewStepEvent(String stepType) {
        this.stepType = stepType;
    }

    public String getStepType() {
        return stepType;
    }
}
