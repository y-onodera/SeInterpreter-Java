package com.sebuilder.interpreter.factory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sebuilder.interpreter.Script;
import com.sebuilder.interpreter.Suite;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

public class SuiteBuilder {
    private final File suiteFile;
    private final ScriptFactory scriptFactory;
    private final ArrayList<Script> scripts;
    private final Map<Script, Script> scriptChains;
    private boolean shareState = true;

    public static SuiteBuilder loadScript(ScriptFactory scriptFactory, File file) throws IOException, JSONException {
        SuiteBuilder result = new SuiteBuilder(scriptFactory, file);
        result.scripts.addAll(Lists.newArrayList(scriptFactory.parse(file)));
        return result;
    }

    public SuiteBuilder(ScriptFactory scriptFactory, File suiteFile) {
        this.scriptFactory = scriptFactory;
        this.suiteFile = suiteFile;
        this.scripts = new ArrayList<>();
        this.scriptChains = Maps.newHashMap();
    }

    public SuiteBuilder setShareState(boolean shareState) {
        this.shareState = shareState;
        return this;
    }

    public SuiteBuilder addScripts(JSONObject o) throws IOException, JSONException {
        JSONArray scriptLocations = o.getJSONArray("scripts");
        for (int i = 0; i < scriptLocations.length(); i++) {
            JSONObject script = scriptLocations.getJSONObject(i);
            if (script.has("path")) {
                this.addScript(script);
            } else if (script.has("paths")) {
                JSONArray scriptArrays = script.getJSONArray("paths");
                this.addScriptChain(scriptArrays);
            }
        }
        return this;
    }

    public SuiteBuilder addScript(JSONObject script) throws IOException, JSONException {
        this.scripts.addAll(Lists.newArrayList(this.loadScript(script)));
        return this;
    }

    public SuiteBuilder addScriptChain(JSONArray scriptArrays) throws JSONException, IOException {
        Script lastLoad = null;
        for (int j = 0; j < scriptArrays.length(); j++) {
            for (Script loaded : this.loadScript(scriptArrays.getJSONObject(j))) {
                if (lastLoad != null) {
                    this.scriptChains.put(lastLoad, loaded);
                }
                this.scripts.add(loaded);
                lastLoad = loaded;
            }
        }
        return this;
    }

    public Suite createSuite() {
        return new Suite(this.suiteFile, this.scripts, this.scriptChains, this.shareState);
    }

    /**
     * @param script A JSONObject describing a script or a suite where load from.
     * @return Script loaded from file
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     * @throws IOException   If script file not found.
     */
    private Suite loadScript(JSONObject script) throws JSONException, IOException {
        String path = script.getString("path");
        if (script.has("where") && Strings.isNullOrEmpty(script.getString("where"))) {
            File wherePath = new File(script.getString("where"), path);
            return this.loadScriptIfExists(wherePath);
        }
        File f = new File(path);
        if (!f.exists()) {
            f = new File(this.suiteFile.getAbsoluteFile().getParentFile(), path);
        }
        return this.loadScriptIfExists(f);
    }

    /**
     * @param wherePath file script load from
     * @return Script loaded from file
     * @throws IOException   If script file not found.
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private Suite loadScriptIfExists(File wherePath) throws IOException, JSONException {
        if (wherePath.exists()) {
            return this.scriptFactory.parse(wherePath);
        }
        throw new IOException("Script file " + wherePath.toString() + " not found.");
    }

}