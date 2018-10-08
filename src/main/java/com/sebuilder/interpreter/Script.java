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

package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sebuilder.interpreter.datasource.DataSourceFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * A Selenium 2 script. To create and run a test, instantiate a Script object,
 * add some Script.Steps to its steps, then invoke "run". If you want to be able
 * to run the script step by step, invoke "start", which will return a TestRun
 * object.
 *
 * @author zarkonnen
 */
public class Script {
    public ArrayList<Step> steps = new ArrayList<>();
    public DataSourceFactory dataSourceFactory = new DataSourceFactory();
    public String path = "scriptPath";
    public String name = "scriptName";
    private File relativePath;
    public boolean usePreviousDriverAndVars = false;
    private boolean closeDriver = true;
    private Map<String, String> shareInputs = Maps.newHashMap();
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;
    private Script chainScript;

    public void setDataSource(DataSource dataSource, HashMap<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
    }

    public void stateTakeOver() {
        this.closeDriver = false;
        this.usePreviousDriverAndVars = true;
    }

    public boolean closeDriver() {
        return this.closeDriver;
    }

    public String name() {
        return this.name;
    }

    public List<Map<String, String>> loadData() {
        if (this.dataSource == null) {
            return Lists.newArrayList(new HashMap<>());
        }
        return this.dataSource.getData(this.dataSourceConfig, this.relativePath);
    }

    public void chainTo(Script script) {
        this.chainScript = script;
    }

    public boolean hasChain() {
        return this.chainScript != null;
    }

    public Script associateWith(File target) {
        Script newScript = cloneExcludeStep();
        for (int i = 0, j = this.steps.size(); i < j; i++) {
            newScript.steps.add(this.steps.get(i));
        }
        if (target != null) {
            newScript.name = target.getName();
            newScript.path = target.getPath();
            newScript.relativePath = target.getAbsoluteFile().getParentFile();
        } else {
            newScript.name = "System_in";
            newScript.path = null;
            newScript.relativePath = null;
        }
        return newScript;
    }

    public Script removeStep(int stepIndex) {
        return removeStep(i -> i.intValue() != stepIndex);
    }

    public Script removeStep(Predicate<Number> filter) {
        Script newScript = cloneExcludeStep();
        for (int i = 0, j = this.steps.size(); i < j; i++) {
            if (filter.test(i)) {
                newScript.steps.add(this.steps.get(i));
            }
        }
        return newScript;
    }

    public Script insertStep(int stepIndex, Step newStep) {
        Script newScript = cloneExcludeStep();
        for (int i = 0, j = this.steps.size(); i < j; i++) {
            if (i == stepIndex) {
                newScript.steps.add(newStep);
            }
            newScript.steps.add(this.steps.get(i));
        }
        return newScript;
    }

    public Script addStep(int stepIndex, Step newStep) {
        Script newScript = cloneExcludeStep();
        for (int i = 0, j = this.steps.size(); i < j; i++) {
            newScript.steps.add(this.steps.get(i));
            if (i == stepIndex) {
                newScript.steps.add(newStep);
            }
        }
        return newScript;
    }

    public Script replaceStep(int stepIndex, Step newStep) {
        Script newScript = cloneExcludeStep();
        for (int i = 0, j = this.steps.size(); i < j; i++) {
            if (i != stepIndex) {
                newScript.steps.add(this.steps.get(i));
            } else {
                newScript.steps.add(newStep);
            }
        }
        return newScript;
    }

    @Override
    public String toString() {
        try {
            return toJSON().toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        JSONArray stepsA = new JSONArray();
        for (Step s : steps) {
            stepsA.put(s.toJSON());
        }
        o.put("steps", stepsA);
        if (this.dataSource != null) {
            JSONObject data = new JSONObject();
            final String sourceName = this.dataSource.getClass().getSimpleName().toLowerCase();
            data.put("source", sourceName);
            JSONObject configs = new JSONObject();
            configs.put(sourceName, this.dataSourceConfig);
            data.put("configs", configs);
            o.put("data", data);
        }
        return o;
    }

    private Script cloneExcludeStep() {
        Script newScript = new Script();
        newScript.dataSource = this.dataSource;
        newScript.dataSourceConfig = this.dataSourceConfig;
        newScript.path = this.path;
        newScript.name = this.name;
        newScript.relativePath = this.relativePath;
        newScript.usePreviousDriverAndVars = this.usePreviousDriverAndVars;
        newScript.closeDriver = this.closeDriver;
        newScript.shareInputs = this.shareInputs;
        return newScript;
    }

    private void addShareInputs(Map<String, String> data) {
        for (Map.Entry<String, String> entry : this.shareInputs.entrySet()) {
            if (!data.containsKey(entry.getKey())) {
                data.put(entry.getKey(), entry.getValue());
            }
        }
    }

}