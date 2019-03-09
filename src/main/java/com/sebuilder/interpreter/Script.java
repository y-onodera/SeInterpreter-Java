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
import java.util.*;
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
public class Script implements TestRunnable {
    private final ArrayList<Step> steps;
    private final String path;
    private final String name;
    private final File relativePath;
    private final boolean usePreviousDriverAndVars;
    private final boolean closeDriver;
    private final DataSet dataSet;
    private final DataSet overrideDataSet;
    private final String skip;

    public Script(ScriptBuilder scriptBuilder) {
        this.steps = scriptBuilder.getSteps();
        this.path = scriptBuilder.getPath();
        this.name = scriptBuilder.getName();
        this.relativePath = scriptBuilder.getRelativePath();
        this.usePreviousDriverAndVars = scriptBuilder.isUsePreviousDriverAndVars();
        this.closeDriver = scriptBuilder.isCloseDriver();
        this.dataSet = scriptBuilder.getDataSet();
        this.overrideDataSet = scriptBuilder.getOverrideDataSet();
        this.skip = scriptBuilder.getSkip();
    }

    @Override
    public void accept(TestRunner runner, SeInterpreterTestListener testListener) {
        runner.execute(this, testListener);
    }

    public ScriptBuilder builder() {
        return new ScriptBuilder(this);
    }

    public List<Step> steps() {
        return Collections.unmodifiableList(this.steps);
    }

    public boolean closeDriver() {
        return this.closeDriver;
    }

    public boolean usePreviousDriverAndVars() {
        return this.usePreviousDriverAndVars;
    }

    public File relativePath() {
        return this.relativePath;
    }

    public String name() {
        return this.name;
    }

    public String path() {
        return Optional.ofNullable(this.path).orElse("");
    }

    public DataSource dataSource() {
        return this.dataSet.getDataSource();
    }

    public Map<String, String> dataSourceConfig() {
        return this.dataSet.getDataSourceConfig();
    }

    public DataSource overrideDataSource() {
        return overrideDataSet.getDataSource();
    }

    public Map<String, String> overrideDataSourceConfig() {
        return overrideDataSet.getDataSourceConfig();
    }

    public String skip() {
        return skip;
    }

    public List<Map<String, String>> loadData() {
        if (this.overrideDataSource() != null) {
            return this.overrideDataSet.loadData();
        }
        return this.dataSet.loadData();
    }

    public boolean skipRunning(Map<String, String> dataSource) {
        return Boolean.valueOf(TestRuns.replaceVars(this.skip, dataSource));
    }

    public Script rename(String aName) {
        return this.builder()
                .setName(aName)
                .createScript();
    }

    public Script usePreviousDriverAndVars(boolean userPreviousDriverAndVars) {
        return this.builder()
                .usePreviousDriverAndVars(userPreviousDriverAndVars)
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
            if (this.steps.size() == 0) {
                return Lists.newArrayList(newStep);
            }
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
            if (this.steps.size() == 0) {
                return Lists.newArrayList(newStep);
            }
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

    public Script addStep(Script export) {
        ArrayList newStep = Lists.newArrayList(this.steps);
        newStep.addAll(export.steps);
        return this.replaceStep(newStep);
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

    public Script skip(String skip) {
        return this.builder()
                .setSkip(skip)
                .createScript();
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
        JSONObject data = this.dataSet.toJSON();
        if (data != null) {
            o.put("data", data);
        }
        return o;
    }

    private Script replaceStep(ArrayList<Step> newStep) {
        return new ScriptBuilder(this)
                .clearStep()
                .addSteps(newStep)
                .createScript();
    }

}