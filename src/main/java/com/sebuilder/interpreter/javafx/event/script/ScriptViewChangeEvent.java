package com.sebuilder.interpreter.javafx.event.script;

import com.sebuilder.interpreter.javafx.event.ViewType;

public class ScriptViewChangeEvent {
    private final ViewType viewType;

    public ScriptViewChangeEvent(ViewType viewType) {
        this.viewType = viewType;
    }

    public ViewType getViewType() {
        return viewType;
    }
}
