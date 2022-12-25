package com.sebuilder.interpreter;

import java.io.File;

public interface ScriptParser {

    default String type() {
        return this.getClass().getSimpleName();
    }

    TestCase load(File f);

    TestCase load(String jsonString);

    TestCase load(String jsonString, File file);

    Aspect loadAspect(File f);
}
