package com.sebuilder.interpreter.javafx.event.view;

import com.sebuilder.interpreter.Script;

public class RefreshStepTableViewEvent {

    private final Script script;

    public RefreshStepTableViewEvent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }
}
