package com.sebuilder.interpreter.javafx.event.file;

import java.io.File;

public class FileSaveSuiteEvent {
    private final File file;

    public FileSaveSuiteEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
