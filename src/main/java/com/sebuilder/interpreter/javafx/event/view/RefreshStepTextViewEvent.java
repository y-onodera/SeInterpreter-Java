package com.sebuilder.interpreter.javafx.event.view;

import com.sebuilder.interpreter.Script;

public class RefreshStepTextViewEvent {

    private final Script script;

    public RefreshStepTextViewEvent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }
}
