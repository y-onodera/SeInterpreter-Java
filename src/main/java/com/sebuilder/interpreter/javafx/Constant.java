package com.sebuilder.interpreter.javafx;

import javafx.scene.input.DataFormat;

public enum Constant {
    SINGLETON;
    public static final DataFormat SERIALIZED_MIME_TYPE = new DataFormat("application/x-java-serialized-object");
    public static final String DROP_HINT_STYLE = "-fx-border-color: #eea82f; -fx-border-width: 0 0 2 0; -fx-padding: 3 3 1 3";
}
