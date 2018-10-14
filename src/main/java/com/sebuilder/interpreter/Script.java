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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Selenium 2 script. To create and run a test, instantiate a Script object,
 * add some Script.Steps to its steps, then invoke "run". If you want to be able
 * to run the script step by step, invoke "start", which will return a TestRun
 * object.
 *
 * @author zarkonnen
 */
public class Script {
    public ArrayList<Step> steps;
    public final String path;
    public final String name;
    public final File relativePath;
    public final boolean usePreviousDriverAndVars;
    public final boolean closeDriver;
    public final Map<String, String> shareInputs;
    public final DataSource dataSource;
    public final Map<String, String> dataSourceConfig;

    public Script(ArrayList<Step> steps
            , String path
            , String name
            , File relativePath
            , boolean usePreviousDriverAndVars
            , boolean closeDriver
            , Map<String, String> shareInputs
            , DataSource dataSource
            , Map<String, String> dataSourceConfig) {
        this.steps = steps;
        this.path = path;
        this.name = name;
        this.relativePath = relativePath;
        this.usePreviousDriverAndVars = usePreviousDriverAndVars;
        this.closeDriver = closeDriver;
        this.shareInputs = shareInputs;
        this.dataSource = dataSource;
        this.dataSourceConfig = dataSourceConfig;
    }

    public ScriptBuilder builder() {
        return new ScriptBuilder(this);
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

    public Script reusePreviousDriverAndVars() {
        return this.builder()
                .reusePreviousDriverAndVars()
                .createScript();
    }

    public Script editStep(Function<ArrayList<Step>, ArrayList<Step>> converter) {
        return this.replaceStep(converter.apply(this.steps));
    }

    public Script filterStep(Predicate<Step> filter) {
        return this.editStep(it -> it.stream()
                .filter(filter)
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    public Script copy() {
        return this.editStep((ArrayList<Step> it) -> it.stream()
                .map(step -> step.copy())
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public Script removeStep(int stepIndex) {
        return removeStep(i -> i.intValue() != stepIndex);
    }

    public Script removeStep(Predicate<Number> filter) {
        return this.editStep(it -> {
                    final ArrayList<Step> newSteps = new ArrayList<>();
                    for (int i = 0, j = this.steps.size(); i < j; i++) {
                        if (filter.test(i)) {
                            newSteps.add(this.steps.get(i));
                        }
                    }
                    return newSteps;
                }
        );
    }

    public Script insertStep(int stepIndex, Step newStep) {
        return this.editStep(it -> {
                    final ArrayList<Step> newSteps = new ArrayList<>();
                    for (int i = 0, j = this.steps.size(); i < j; i++) {
                        if (i == stepIndex) {
                            newSteps.add(newStep);
                        }
                        newSteps.add(this.steps.get(i));
                    }
                    return newSteps;
                }
        );
    }

    public Script addStep(int stepIndex, Step newStep) {
        return this.editStep(it -> {
                    final ArrayList<Step> newSteps = new ArrayList<>();
                    for (int i = 0, j = this.steps.size(); i < j; i++) {
                        newSteps.add(this.steps.get(i));
                        if (i == stepIndex) {
                            newSteps.add(newStep);
                        }
                    }
                    return newSteps;
                }
        );
    }

    public Script replaceStep(int stepIndex, Step newStep) {
        return this.editStep(it -> {
                    final ArrayList<Step> newSteps = new ArrayList<>();
                    for (int i = 0, j = this.steps.size(); i < j; i++) {
                        if (i != stepIndex) {
                            newSteps.add(this.steps.get(i));
                        } else {
                            newSteps.add(newStep);
                        }
                    }
                    return newSteps;
                }
        );
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

    private Script replaceStep(ArrayList<Step> newStep) {
        return new ScriptBuilder(this)
                .setSteps(newStep)
                .createScript();
    }

}