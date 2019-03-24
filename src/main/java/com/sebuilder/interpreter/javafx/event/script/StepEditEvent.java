package com.sebuilder.interpreter.javafx.event.script;

import com.sebuilder.interpreter.Step;

public class StepEditEvent {

    private final String editAction;

    private final int stepIndex;

    private final Step stepSource;

    public StepEditEvent(String editAction, int stepIndex, Step stepSource) {
        this.editAction = editAction;
        this.stepIndex = stepIndex;
        this.stepSource = stepSource;
    }

    public String getEditAction() {
        return editAction;
    }

    public int getStepIndex() {
        return stepIndex;
    }

    public Step getStepSource() {
        return stepSource;
    }
}
