package com.sebuilder.interpreter.javafx.event.script;

import java.io.File;

public class ScriptImportEvent {
    private final File file;

    public ScriptImportEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
