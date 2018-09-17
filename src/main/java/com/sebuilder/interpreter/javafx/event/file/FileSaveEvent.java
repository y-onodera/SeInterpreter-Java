package com.sebuilder.interpreter.javafx.event.file;

import java.io.File;

public class FileSaveEvent {

    private final File file;

    public FileSaveEvent(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }
}
