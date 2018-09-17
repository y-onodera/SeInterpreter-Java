package com.sebuilder.interpreter.javafx.event.script;


public class SelectScriptEvent {

    private final String scriptName;

    public SelectScriptEvent(String scriptName) {
        this.scriptName = scriptName;
    }

    public String getScriptName() {
        return this.scriptName;
    }

}
