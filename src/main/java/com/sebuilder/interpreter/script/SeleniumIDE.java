package com.sebuilder.interpreter.script;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.Aspect;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseBuilder;
import com.sebuilder.interpreter.TestRunListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.List;

public class SeleniumIDE extends AbstractJsonScriptParser {

    @Override
    public TestCase load(String jsonString) {
        try {
            return new SeleniumIDEConverter().parseTest(new JSONObject(new JSONTokener(jsonString)));
        } catch (JSONException e) {
            throw new AssertionError("error parse:" + jsonString, e);
        }
    }

    @Override
    public Aspect loadAspect(File f) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected TestCase load(JSONObject o, File sourceFile, TestRunListener testRunListener) throws JSONException {
        SeleniumIDEConverter converter = new SeleniumIDEConverter(o);
        return converter.getResult();
    }

}
