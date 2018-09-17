package com.sebuilder.interpreter.javafx.event.file;

import java.io.File;

public class FileLoadEvent {
    private final File file;

    public FileLoadEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
