package com.sebuilder.interpreter.javafx.event.script;

import org.json.JSONObject;

public class StepEditEvent {

    private final String editAction;

    private final int stepIndex;

    private final JSONObject stepSource;

    public StepEditEvent(String editAction, int stepIndex, JSONObject stepSource) {
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

    public JSONObject getStepSource() {
        return stepSource;
    }
}
