package com.sebuilder.interpreter.javafx.event.script;

import com.sebuilder.interpreter.Script;

public class ScriptReloadEvent {

    private final Script script;

    public ScriptReloadEvent(Script script) {
        this.script = script;
    }

    public Script getScript() {
        return script;
    }
}
