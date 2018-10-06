package com.sebuilder.interpreter.javafx.event.script;

import com.sebuilder.interpreter.Script;

public class ScriptAddEvent {
    private final Script script;

    public ScriptAddEvent() {
        this(null);
    }

    public ScriptAddEvent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }
}
