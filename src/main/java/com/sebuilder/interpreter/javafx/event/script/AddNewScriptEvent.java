package com.sebuilder.interpreter.javafx.event.script;

import com.sebuilder.interpreter.Script;

public class AddNewScriptEvent {

    private final Script script;

    public AddNewScriptEvent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return this.script;
    }
}
