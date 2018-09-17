package com.sebuilder.interpreter.javafx.event.view;

import com.sebuilder.interpreter.Script;

public class RefreshStepViewEvent {

    private final Script script;

    public RefreshStepViewEvent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }
}
