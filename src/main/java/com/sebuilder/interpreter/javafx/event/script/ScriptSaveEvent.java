package com.sebuilder.interpreter.javafx.event.script;

import java.io.File;

public class ScriptSaveEvent {
    private final File file;

    public ScriptSaveEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
