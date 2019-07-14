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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Selenium 2 script. To create and run a test, instantiate a TestCase object,
 * appendNewChain some TestCase.Steps to its steps, then invoke "run". If you want to be able
 * to run the script step by step, invoke "start", which will return a TestRun
 * object.
 *
 * @author zarkonnen
 */
public class TestCase implements TestRunnable {
    public static final String DEFAULT_SCRIPT_NAME = "New_Script";
    private final ArrayList<Step> steps;
    private final Function<TestData, TestCase> lazyLoad;
    private final boolean usePreviousDriverAndVars;
    private final boolean closeDriver;
    private final DataSet dataSet;
    private final DataSet overrideDataSet;
    private final String skip;
    private final ScriptFile scriptFile;
    private boolean nestedChain;

    public TestCase(TestCaseBuilder testCaseBuilder) {
        this.steps = testCaseBuilder.getSteps();
        this.lazyLoad = testCaseBuilder.getLazyLoad();
        this.scriptFile = testCaseBuilder.getScriptFile();
        this.usePreviousDriverAndVars = testCaseBuilder.isUsePreviousDriverAndVars();
        this.closeDriver = testCaseBuilder.isCloseDriver();
        this.dataSet = testCaseBuilder.getDataSet();
        this.overrideDataSet = testCaseBuilder.getOverrideDataSet();
        this.skip = testCaseBuilder.getSkip();
        this.nestedChain = testCaseBuilder.isNestedChain();
    }

    @Override
    public void accept(TestRunner runner, TestRunListener testListener) {
        runner.execute(this, testListener);
    }

    public TestCaseBuilder builder() {
        return new TestCaseBuilder(this);
    }

    public ArrayList<Step> steps() {
        return Lists.newArrayList(this.steps);
    }

    public boolean closeDriver() {
        return this.closeDriver;
    }

    public boolean usePreviousDriverAndVars() {
        return this.usePreviousDriverAndVars;
    }

    public ScriptFile testCase() {
        return this.scriptFile;
    }

    public File relativePath() {
        return this.scriptFile.relativePath();
    }

    public String name() {
        return this.scriptFile.name();
    }

    public Function<TestData, TestCase> lazyLoad() {
        return this.lazyLoad;
    }

    public TestCase lazyLoad(TestData it) {
        if (this.isLazyLoad()) {
            return this.lazyLoad.apply(it)
                    .builder()
                    .overrideDataSource(this.overrideDataSource(), this.overrideDataSourceConfig())
                    .setSkip(this.skip)
                    .build();
        }
        return this;
    }

    public String path() {
        return this.scriptFile.path();
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public DataSource dataSource() {
        return this.dataSet.getDataSource();
    }

    public Map<String, String> dataSourceConfig() {
        return this.dataSet.getDataSourceConfig();
    }

    public DataSet getOverrideDataSet() {
        return overrideDataSet;
    }

    public DataSource overrideDataSource() {
        return this.overrideDataSet.getDataSource();
    }

    public Map<String, String> overrideDataSourceConfig() {
        return this.overrideDataSet.getDataSourceConfig();
    }

    public List<TestData> loadData(TestData vars) {
        if (this.overrideDataSource() != null) {
            return this.overrideDataSet.loadData(vars);
        }
        return this.dataSet.loadData();
    }

    public boolean isNestedChain() {
        return this.nestedChain;
    }

    public boolean isLazyLoad() {
        return this.lazyLoad != null;
    }

    public String skip() {
        return this.skip;
    }

    public boolean skipRunning(TestData testData) {
        return testData.evaluate(this.skip);
    }

    public TestCase rename(String aName) {
        return this.builder()
                .setName(aName)
                .build();
    }

    public TestCase changeDataSourceConfig(String key, String value) {
        TestCaseBuilder builder = this.builder();
        builder.getDataSourceConfig().put(key, value);
        return builder.build();
    }

    public TestCase usePreviousDriverAndVars(boolean userPreviousDriverAndVars) {
        return this.builder()
                .usePreviousDriverAndVars(userPreviousDriverAndVars)
                .build();
    }

    public TestCase editStep(Function<ArrayList<Step>, ArrayList<Step>> converter) {
        return this.replaceStep(converter.apply(this.steps));
    }

    public TestCase filterStep(Predicate<Step> filter) {
        return this.editStep(it -> it.stream()
                .filter(filter)
                .collect(Collectors.toCollection(ArrayList::new))
        );
    }

    public TestCase copy() {
        return this.editStep((ArrayList<Step> it) -> it.stream()
                .map(step -> step.copy())
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public TestCase removeStep(int stepIndex) {
        return removeStep(i -> i.intValue() != stepIndex);
    }

    public TestCase removeStep(Predicate<Number> filter) {
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

    public TestCase insertStep(int stepIndex, Step newStep) {
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

    public TestCase addStep(int stepIndex, Step newStep) {
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

    public TestCase addStep(TestCase export) {
        ArrayList newStep = Lists.newArrayList(this.steps);
        newStep.addAll(export.steps);
        return this.replaceStep(newStep);
    }

    public TestCase replaceStep(ArrayList<Step> newStep) {
        return new TestCaseBuilder(this)
                .clearStep()
                .addSteps(newStep)
                .build();
    }

    public TestCase replaceStep(int stepIndex, Step newStep) {
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

    public TestCase skip(String skip) {
        return this.builder()
                .setSkip(skip)
                .build();
    }

    public TestCase nestedChain(boolean nestedChain) {
        return this.builder()
                .isNestedChain(nestedChain)
                .build();
    }

    public Suite toSuite() {
        return new SuiteBuilder(this).createSuite();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return usePreviousDriverAndVars == testCase.usePreviousDriverAndVars &&
                closeDriver == testCase.closeDriver &&
                com.google.common.base.Objects.equal(steps, testCase.steps) &&
                com.google.common.base.Objects.equal(isLazyLoad(), testCase.isLazyLoad()) &&
                com.google.common.base.Objects.equal(dataSet, testCase.dataSet) &&
                com.google.common.base.Objects.equal(overrideDataSet, testCase.overrideDataSet) &&
                com.google.common.base.Objects.equal(skip, testCase.skip) &&
                com.google.common.base.Objects.equal(scriptFile, testCase.scriptFile);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(steps, isLazyLoad(), usePreviousDriverAndVars, closeDriver, dataSet, overrideDataSet, skip, scriptFile);
    }
}