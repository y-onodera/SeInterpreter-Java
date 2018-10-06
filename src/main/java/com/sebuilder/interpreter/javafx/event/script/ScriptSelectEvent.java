package com.sebuilder.interpreter.javafx.event.script;


public class ScriptSelectEvent {

    private final String scriptName;

    public ScriptSelectEvent(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptName() {
        return this.scriptName;
    }

}
