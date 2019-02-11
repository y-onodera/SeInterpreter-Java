package com.sebuilder.interpreter.javafx.event.script;

public class ScriptReplaceEvent {
    private final String script;

    public ScriptReplaceEvent(String script) {
        this.script = script;
    }

    public String getScript() {
        return script;
    }
}
