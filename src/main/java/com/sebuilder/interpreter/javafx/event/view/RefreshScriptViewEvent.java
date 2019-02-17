package com.sebuilder.interpreter.javafx.event.view;

import com.sebuilder.interpreter.Suite;

public class RefreshScriptViewEvent {

    private final Suite suite;

    private final String selectScriptName;

    public RefreshScriptViewEvent(Suite suite, String selectScriptName) {
        this.selectScriptName = selectScriptName;
        this.suite = suite;
    }

    public Suite getSuite() {
        return this.suite;
    }

    public String getSelectScriptName() {
        return selectScriptName;
    }
}
