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

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.DataSourceFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Factory to create Script objects from a string, a reader or JSONObject.
 *
 * @author jkowalczyk
 */
public class ScriptFactory {

    private StepTypeFactory stepTypeFactory = new StepTypeFactory();

    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    public void setStepTypeFactory(StepTypeFactory stepTypeFactory) {
        this.stepTypeFactory = stepTypeFactory;
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
     * @param jsonString A JSON string describing a script or suite.
     * @return A script, ready to run.
     * @throws IOException   If anything goes wrong with interpreting the JSON, or
     *                       with the Reader.
     * @throws JSONException If the JSON can't be parsed.
     */
    public Script parse(String jsonString) throws IOException, JSONException {
        return this.parse(new JSONObject(new JSONTokener(jsonString)), null).iterator().next();
    }

    /**
     * @param f A File pointing to a JSON file describing a script or suite.
     * @return A list of scripts, ready to run.
     * @throws IOException   If anything goes wrong with interpreting the JSON, or
     *                       with the Reader.
     * @throws JSONException If the JSON can't be parsed.
     */
    public Suite parse(File f) throws IOException, JSONException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            return this.parse(r, f);
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
    public Suite parse(Reader reader, File sourceFile) throws IOException, JSONException {
        return this.parse(new JSONObject(new JSONTokener(reader)), sourceFile);
    }

    /**
     * @param o          A JSONObject describing a script or a suite.
     * @param sourceFile Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    public Suite parse(JSONObject o, File sourceFile) throws IOException {
        if (o.optString("type", "script").equals("suite")) {
            return this.parseSuite(o, sourceFile);
        }
        return this.parseScript(o, sourceFile);
    }

    /**
     * @param o         A JSONObject describing a script or a suite.
     * @param suiteFile Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    public Suite parseSuite(JSONObject o, File suiteFile) throws IOException {
        try {
            return new SuiteBuilder(this, suiteFile)
                    .setShareState(o.optBoolean("shareState", true))
                    .addScripts(o)
                    .createSuite();
        } catch (JSONException e) {
            throw new IOException("Could not parse suite.", e);
        }
    }

    /**
     * @param o A JSONObject describing a script or a suite.
     * @param f Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    public Suite parseScript(JSONObject o, File f) throws IOException {
        try {
            Script script = this.create(f);
            JSONArray stepsA = o.getJSONArray("steps");
            this.parseStep(script, stepsA);
            this.parseData(o, script);
            return new Suite(script);
        } catch (JSONException e) {
            throw new IOException("Could not parse script.", e);
        }
    }

    /**
     * @param saveTo file script save to
     * @return A new instance of script
     */
    private Script create(File saveTo) {
        Script script = new Script();
        script.dataSourceFactory = this.dataSourceFactory;
        return script.associateWith(saveTo);
    }


    /**
     * @param script script step set to
     * @param stepsA json object step load from
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private void parseStep(Script script, JSONArray stepsA) throws JSONException {
        for (int i = 0; i < stepsA.length(); i++) {
            this.parseStep(script, stepsA.getJSONObject(i));
        }
    }

    /**
     * @param script script step set to
     * @param stepO  json object step load from
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private void parseStep(Script script, JSONObject stepO) throws JSONException {
        Step step = this.createStep(stepO);
        script.steps.add(step);
        this.configureStep(script, stepO, step);
    }

    /**
     * @param stepO json object step load from
     * @return A new instance of step
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private Step createStep(JSONObject stepO) throws JSONException {
        StepType type = this.stepTypeFactory.getStepTypeOfName(stepO.getString("type"));
        Step step = new Step(type);
        step.negated = stepO.optBoolean("negated", false);
        step.name = stepO.optString("step_name", null);
        return step;
    }

    /**
     * @param script script step belong with
     * @param stepO  json object step configuration load from
     * @param step   step configuration to
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private void configureStep(Script script, JSONObject stepO, Step step) throws JSONException {
        JSONArray keysA = stepO.names();
        for (int j = 0; j < keysA.length(); j++) {
            this.configureStep(script, stepO, step, keysA.getString(j));
        }
    }

    /**
     * @param script script step belong with
     * @param stepO  json object step configuration load from
     * @param step   step configuration to
     * @param key    configuration key
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private void configureStep(Script script, JSONObject stepO, Step step, String key) throws JSONException {
        if (key.equals("type") || key.equals("negated")) {
            return;
        }
        if (stepO.optJSONObject(key) != null) {
            this.configureStepSubElement(script, stepO, step, key);
        } else if (key.equals("actions")) {
            this.configureStepSubElement(script, stepO, step, key);
        } else {
            step.stringParams.put(key, stepO.getString(key));
        }
    }

    /**
     * @param script script step belong with
     * @param stepO  json object step configuration load from
     * @param step   step configuration to
     * @param key    configuration key
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    private void configureStepSubElement(Script script, JSONObject stepO, Step step, String key) throws JSONException {
        switch (key) {
            case "locator":
                step.locatorParams.put(key, new Locator(stepO.getJSONObject(key).getString("type"), stepO.getJSONObject(key).getString("value")));
                break;
            case "until":
                this.parseStep(script, stepO.getJSONObject(key));
                break;
            case "actions":
                JSONArray actions = stepO.getJSONArray(key);
                step.stringParams.put("subStep", String.valueOf(actions.length()));
                for (int i = 0, j = actions.length(); i < j; i++) {
                    this.parseStep(script, actions.getJSONObject(i));
                }
                break;
            default:
        }
    }

    /**
     * @param o      json object dataSource configuration load from
     * @param script dataSource set to
     * @throws JSONException If anything goes wrong with interpreting the JSON.
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
}