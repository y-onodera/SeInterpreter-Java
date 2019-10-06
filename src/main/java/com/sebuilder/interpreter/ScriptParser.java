package com.sebuilder.interpreter;

import java.io.File;
import java.io.IOException;

public interface ScriptParser {

    default String type() {
        return this.getClass().getSimpleName();
    }

    String toString(Suite target);

    String toString(TestCase target);

    TestRunnable load(File f) throws IOException;

    TestRunnable load(String json, File file) throws IOException;

    TestCase load(String jsonString);

    Aspect loadAspect(File f) throws IOException;
}
