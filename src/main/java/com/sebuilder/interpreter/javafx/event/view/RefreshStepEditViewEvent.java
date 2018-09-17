package com.sebuilder.interpreter.javafx.event.view;

import com.sebuilder.interpreter.Step;

public class RefreshStepEditViewEvent {
    private final Step step;

    private final String editAction;

    private RefreshStepEditViewEvent(String edit, Step step) {
        this.step = step;
        this.editAction = edit;
    }

    public static RefreshStepEditViewEvent add(Step step) {
        return new RefreshStepEditViewEvent("add", step);
    }

    public static RefreshStepEditViewEvent change(Step step) {
        return new RefreshStepEditViewEvent("change", step);
    }

    public String getEditAction() {
        return this.editAction;
    }

    public Step getStep() {
        return this.step;
    }

}

