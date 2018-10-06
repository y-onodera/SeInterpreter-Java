package com.sebuilder.interpreter.javafx.event.file;

import java.io.File;

public class FileSaveAsEvent {

    private final File file;

    public FileSaveAsEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
