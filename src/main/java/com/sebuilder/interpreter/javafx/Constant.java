package com.sebuilder.interpreter.javafx;

import javafx.scene.input.DataFormat;

public enum Constant {
    SINGLETON;
    public static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
}
