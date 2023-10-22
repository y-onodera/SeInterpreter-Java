package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Aspect;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.script.AbstractJsonScriptParser;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;

public class SeleniumIDE extends AbstractJsonScriptParser {

    @Override
    public TestCase load(final String jsonString) {
        try {
            return new SeleniumIDEConverter().parseTest(new JSONObject(new JSONTokener(jsonString)));
        } catch (final Throwable e) {
            throw new AssertionError("error parse:" + jsonString, e);
        }
    }

    @Override
    protected TestCase load(final JSONObject o, final File sourceFile) {
        final SeleniumIDEConverter converter = new SeleniumIDEConverter(o);
        return converter.getResult();
    }

    @Override
    public Aspect loadAspect(final File f) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Aspect loadAspect(final String jsonText, final File f) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Pointcut loadPointCut(final File path, final File parentDir) {
        throw new UnsupportedOperationException();
    }
}
