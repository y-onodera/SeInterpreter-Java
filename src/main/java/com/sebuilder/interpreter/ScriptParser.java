package com.sebuilder.interpreter;

import java.io.File;
import java.io.IOException;

public interface ScriptParser {

    default String type() {
        return this.getClass().getSimpleName();
    }

    String toString(Suite target);

    String toString(TestCase target);

    TestCase load(File f) throws IOException;

    TestCase load(String jsonString);

    TestCase load(String jsonString, File file) throws IOException;

    Aspect loadAspect(File f) throws IOException;
}
