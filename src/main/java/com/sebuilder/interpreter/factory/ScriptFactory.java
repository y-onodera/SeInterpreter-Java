/*
 * Copyright 2012 Sauce Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sebuilder.interpreter.factory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Factory to create Script objects from a string, a reader or JSONObject.
 *
 * @author jkowalczyk
 */
public class ScriptFactory {
    private StepTypeFactory stepTypeFactory = new StepTypeFactory();
    private TestRunFactory testRunFactory = new TestRunFactory();
    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    public void setStepTypeFactory(StepTypeFactory stepTypeFactory) {
        this.stepTypeFactory = stepTypeFactory;
    }

    public void setTestRunFactory(TestRunFactory testRunFactory) {
        this.testRunFactory = testRunFactory;
    }

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    public Script template(String stepType) throws JSONException, IOException {
        return this.parse("{\"steps\":[{\"type\":\"" + stepType + "\"}]}");
    }

    public Script open(String url) throws JSONException, IOException {
        return this.parse("{\"steps\":[" + "{\"type\":\"get\",\"url\":\"" + url + "\"}" + "]}");
    }

    public Script highLightElement(String locatorType, String value) throws JSONException, IOException {
        JSONObject json = new JSONObject();
        JSONArray steps = new JSONArray();
        JSONObject step = new JSONObject();
        JSONObject locator = new JSONObject();
        locator.put("type", locatorType);
        locator.put("value", value);
        step.put("type", "highLightElement");
        step.putOpt("locator", locator);
        steps.put(step);
        json.putOpt("steps", steps);
        return this.parseScript(json, null).iterator().next();
    }

    /**
     * @param f A File pointing to a JSON file describing a script or suite.
     * @return A list of scripts, ready to run.
     * @throws IOException   If anything goes wrong with interpreting the JSON, or
     *                       with the Reader.
     * @throws JSONException If the JSON can't be parsed.
     */
    public Iterable<Script> parse(File f) throws IOException, JSONException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            return parse(r, f);
        }
    }

    /**
     * @param reader     A Reader pointing to a JSON stream describing a script or suite.
     * @param sourceFile Optionally. the file the JSON was loaded from.
     * @return A list of scripts, ready to run.
     * @throws IOException   If anything goes wrong with interpreting the JSON, or
     *                       with the Reader.
     * @throws JSONException If the JSON can't be parsed.
     */
    public Iterable<Script> parse(Reader reader, File sourceFile) throws IOException, JSONException {
        return parse(new JSONObject(new JSONTokener(reader)), sourceFile);
    }

    /**
     * @param jsonString A JSON string describing a script or suite.
     * @return A script, ready to run.
     * @throws IOException   If anything goes wrong with interpreting the JSON, or
     *                       with the Reader.
     * @throws JSONException If the JSON can't be parsed.
     */
    public Script parse(String jsonString) throws IOException, JSONException {
        return parse(new JSONObject(new JSONTokener(jsonString)), null).iterator().next();
    }


    /**
     * @param o          A JSONObject describing a script or a suite.
     * @param sourceFile Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    public Iterable<Script> parse(JSONObject o, File sourceFile) throws IOException {
        if (o.optString("type", "script").equals("suite")) {
            return parseSuite(o, sourceFile);
        }
        return parseScript(o, sourceFile);
    }

    /**
     * @param o
     * @param f
     * @return
     * @throws IOException
     */
    public List<Script> parseScript(JSONObject o, File f) throws IOException {
        try {
            Script script = this.create(f);
            JSONArray stepsA = o.getJSONArray("steps");
            parseStep(script, stepsA);
            parseData(o, script);
            return Lists.newArrayList(script);
        } catch (JSONException e) {
            throw new IOException("Could not parse script.", e);
        }
    }

    /**
     * @param f
     * @return A new instance of script
     */
    private Script create(File f) {
        Script script = new Script();
        script.testRunFactory = testRunFactory;
        script.dataSourceFactory = this.dataSourceFactory;
        return script.associateWith(f);
    }


    /**
     * @param script
     * @throws JSONException
     */
    private void parseStep(Script script, JSONArray stepsA) throws JSONException {
        for (int i = 0; i < stepsA.length(); i++) {
            parseStep(script, stepsA.getJSONObject(i));
        }
    }

    private void parseStep(Script script, JSONObject stepO) throws JSONException {
        Step step = createStep(stepO);
        script.steps.add(step);
        configureStep(script, stepO, step);
    }

    /**
     * @param stepO
     * @return
     * @throws JSONException
     */
    private Step createStep(JSONObject stepO) throws JSONException {
        StepType type = stepTypeFactory.getStepTypeOfName(stepO.getString("type"));
        Step step = new Step(type);
        step.negated = stepO.optBoolean("negated", false);
        step.name = stepO.optString("step_name", null);
        return step;
    }

    /**
     * @param script
     * @param stepO
     * @param step
     * @throws JSONException
     */
    private void configureStep(Script script, JSONObject stepO, Step step) throws JSONException {
        JSONArray keysA = stepO.names();
        for (int j = 0; j < keysA.length(); j++) {
            configureStep(script, stepO, step, keysA.getString(j));
        }
    }

    private void configureStep(Script script, JSONObject stepO, Step step, String key) throws JSONException {
        if (key.equals("type") || key.equals("negated")) {
            return;
        }
        if (stepO.optJSONObject(key) != null) {
            configureStepSubElement(script, stepO, step, key);
        } else if (key.equals("actions")) {
            configureStepSubElement(script, stepO, step, key);
        } else {
            step.stringParams.put(key, stepO.getString(key));
        }
    }

    private void configureStepSubElement(Script script, JSONObject stepO, Step step, String key) throws JSONException {
        if (key.equals("locator")) {
            step.locatorParams.put(key, new Locator(stepO.getJSONObject(key).getString("type"), stepO.getJSONObject(key).getString("value")));
        } else if (key.equals("until")) {
            this.parseStep(script, stepO.getJSONObject(key));
        } else if (key.equals("actions")) {
            JSONArray actions = stepO.getJSONArray(key);
            step.stringParams.put("subStep", String.valueOf(actions.length()));
            for (int i = 0, j = actions.length(); i < j; i++) {
                this.parseStep(script, actions.getJSONObject(i));
            }
        }
    }

    /**
     * @param o
     * @param script
     * @throws JSONException
     */
    private void parseData(JSONObject o, Script script) throws JSONException {
        if (!o.has("data")) {
            return;
        }
        JSONObject data = o.getJSONObject("data");
        String sourceName = data.getString("source");
        HashMap<String, String> config = new HashMap<String, String>();
        if (data.has("configs") && data.getJSONObject("configs").has(sourceName)) {
            JSONObject cfg = data.getJSONObject("configs").getJSONObject(sourceName);
            for (Iterator<String> it = cfg.keys(); it.hasNext(); ) {
                String key = it.next();
                config.put(key, cfg.getString(key));
            }
        }
        script.setDataSource(this.dataSourceFactory.getDataSource(sourceName), config);
    }


    /**
     * @param o
     * @param suiteFile
     * @return
     * @throws IOException
     */
    public Suite parseSuite(JSONObject o, File suiteFile) throws IOException {
        try {
            ArrayList<Script> scripts = collectScripts(o, suiteFile);
            boolean shareState = o.optBoolean("shareState", true);
            if (shareState) {
                shareState(scripts);
            }
            return new Suite(suiteFile, scripts, shareState);
        } catch (JSONException e) {
            throw new IOException("Could not parse suite.", e);
        }
    }

    /**
     * @param o
     * @param suiteFile
     * @return
     * @throws JSONException
     * @throws IOException
     */
    private ArrayList<Script> collectScripts(JSONObject o, File suiteFile) throws JSONException, IOException {
        ArrayList<Script> scripts = new ArrayList<Script>();
        JSONArray scriptLocations = o.getJSONArray("scripts");
        for (int i = 0; i < scriptLocations.length(); i++) {
            JSONObject script = scriptLocations.getJSONObject(i);
            String path = script.getString("path");
            if (script.has("where") && Strings.isNullOrEmpty(script.getString("where"))) {
                File wherePath = new File(script.getString("where"), path);
                if (wherePath.exists()) {
                    parse(wherePath).forEach(it -> scripts.add(it));
                    continue;
                } else {
                    throw new IOException("Script file " + wherePath.toString() + " not found.");
                }
            }
            File f = new File(path);
            if (f.exists()) {
                parse(f).forEach(it -> scripts.add(it));
            } else {
                f = new File(suiteFile.getAbsoluteFile().getParentFile(), path);
                if (f.exists()) {
                    parse(f).forEach(it -> scripts.add(it));
                } else {
                    throw new IOException("Script file " + path + " not found.");
                }
            }
        }
        return scripts;
    }

    /**
     * @param scripts
     */
    private void shareState(ArrayList<Script> scripts) {
        for (Script s : scripts) {
            s.stateTakeOver();
        }
    }

}