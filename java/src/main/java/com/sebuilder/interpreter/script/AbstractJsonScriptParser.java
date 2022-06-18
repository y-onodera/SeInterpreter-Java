package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.ScriptParser;
import com.sebuilder.interpreter.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.charset.StandardCharsets;

public abstract class AbstractJsonScriptParser implements ScriptParser {

    /**
     * @param f               A File pointing to a JSON file describing a script or suite.
     * @return A list of script, ready to finish.
     */
    @Override
    public TestCase load(File f) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            return this.load(new JSONObject(new JSONTokener(r)), f);
        } catch (JSONException | IOException e) {
            throw new IOException("error load:" + f.getAbsolutePath(), e);
        }
    }

    /**
     * @param json            A JSON string describing a script or suite.
     * @return A list of script, ready to finish.
     */
    @Override
    public TestCase load(String json, File file) throws IOException {
        try {
            return this.load(new JSONObject(new JSONTokener(json)), file);
        } catch (JSONException e) {
            throw new IOException("error parse:" + json, e);
        }
    }

    /**
     * @param o               A JSONObject describing a script or a suite.
     * @param sourceFile      Optionally. the file the JSON was loaded from.
     * @return A script, ready to finish.
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    abstract protected TestCase load(JSONObject o, File sourceFile) throws JSONException, IOException;

}
