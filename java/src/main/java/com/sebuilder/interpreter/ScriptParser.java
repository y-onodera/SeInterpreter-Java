package com.sebuilder.interpreter;

import java.io.File;
import java.io.IOException;

public interface ScriptParser {

    default String type() {
        return this.getClass().getSimpleName();
    }

    TestCase load(File f, TestRunListener testRunListener) throws IOException;

    TestCase load(String jsonString);

    TestCase load(String jsonString, File file, TestRunListener testRunListener) throws IOException;

    Aspect loadAspect(File f) throws IOException;
}
