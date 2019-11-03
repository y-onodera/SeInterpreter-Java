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

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A Selenium 2 script. To create and finish a test, instantiate a TestCase object,
 * appendNewChain some TestCase.Steps to its steps, then invoke "finish". If you want to be able
 * to finish the script step by step, invoke "start", which will return a TestRun
 * object.
 *
 * @author zarkonnen
 */
public class TestCase {

    private final ScriptFile scriptFile;
    private final TestData shareInput;
    private final TestDataSet testDataSet;
    private final boolean shareState;
    private final String skip;
    private final TestDataSet overrideTestDataSet;
    private final Function<TestData, TestCase> lazyLoad;
    private final boolean nestedChain;
    private final boolean breakNestedChain;
    private final TestCaseChains chains;
    private final Aspect aspect;
    private final ArrayList<Step> steps;

    public TestCase(TestCaseBuilder builder) {
        this.scriptFile = builder.getScriptFile();
        this.shareInput = builder.getShareInput();
        this.testDataSet = builder.getTestDataSet();
        this.shareState = builder.isShareState();
        this.skip = builder.getSkip();
        this.overrideTestDataSet = builder.getOverrideTestDataSet();
        this.lazyLoad = builder.getLazyLoad();
        this.nestedChain = builder.isNestedChain();
        this.breakNestedChain = builder.isBreakNestedChain();
        this.chains = builder.getChains();
        this.aspect = builder.getAspect();
        this.steps = builder.getSteps();
    }

    public boolean run(TestRunner runner, TestRunListener testRunListener) {
        if (this.skipRunning(this.getShareInput())) {
            return true;
        }
        for (TestRunBuilder testRunBuilder : this.createTestRunBuilder()) {
            for (TestData data : testRunBuilder.loadData()) {
                TestRunner.STATUS result = runner.execute(testRunBuilder.copy(), data, testRunListener);
                if (result == TestRunner.STATUS.STOPPED) {
                    return false;
                }
            }
        }
        return true;
    }

    public List<TestData> loadData(TestData vars) {
        if (this.getOverrideTestDataSet().getDataSource() != null) {
            return this.getOverrideTestDataSet().loadData(vars);
        }
        return this.getTestDataSet().loadData(vars);
    }

    public TestRunBuilder[] createTestRunBuilder() {
        TestCase materialized = this;
        if (this.isLazyLoad()) {
            materialized = this.getLazyLoad().apply(this.getShareInput());
        }
        return new TestRunBuilder[]{new TestRunBuilder(materialized)};
    }

    public Suite toSuite() {
        return new Suite(this);
    }

    public TestCaseBuilder builder() {
        return new TestCaseBuilder(this);
    }

    public ScriptFile getScriptFile() {
        return scriptFile;
    }

    public File relativePath() {
        return this.getScriptFile().relativePath();
    }

    public TestData getShareInput() {
        if (this.shareInput == null) {
            return new TestData();
        }
        return this.shareInput;
    }

    public TestDataSet getTestDataSet() {
        return testDataSet;
    }

    public TestDataSet getOverrideTestDataSet() {
        return overrideTestDataSet;
    }

    public String getSkip() {
        return skip;
    }

    public boolean isLazyLoad() {
        return this.getLazyLoad() != null;
    }

    public Function<TestData, TestCase> getLazyLoad() {
        return lazyLoad;
    }

    public String name() {
        return this.scriptFile.name();
    }

    public String path() {
        return this.scriptFile.path();
    }

    public String fileName() {
        return this.getScriptFile().relativize(this);
    }

    public boolean isShareState() {
        return this.shareState;
    }

    public boolean skipRunning(TestData param) {
        return param.evaluate(this.skip);
    }

    public boolean isNestedChain() {
        return this.nestedChain;
    }

    public boolean isBreakNestedChain() {
        return this.breakNestedChain;
    }

    public boolean hasChain() {
        return this.getChains().size() > 0;
    }

    public TestCaseChains getChains() {
        return this.chains;
    }

    public Aspect getAspect() {
        return this.aspect;
    }

    public ArrayList<Step> steps() {
        return Lists.newArrayList(this.steps);
    }

    public TestCase copy() {
        return this.editStep((ArrayList<Step> it) -> it.stream()
                .map(step -> step.copy())
                .collect(Collectors.toCollection(ArrayList::new)));
    }

    public TestCase changeWhenConditionMatch(Predicate<TestCase> condition, Function<TestCase, TestCase> function) {
        if(condition.test(this)){
            return function.apply(this);
        }
        return this;
    }

    public TestCase changeDataSourceConfig(String key, String value) {
        return this.builder()
                .addDataSourceConfig(key, value)
                .build();
    }

    public TestCase rename(String s) {
        return this.builder()
                .setName(s)
                .build();
    }

    public TestCase shareState(boolean isShareState) {
        return this.builder()
                .isShareState(isShareState)
                .build();
    }

    public TestCase skip(String skip) {
        return this.builder()
                .setSkip(skip)
                .build();
    }

    public TestCase shareInput(TestData testData) {
        return this.builder()
                .setShareInput(testData)
                .build();
    }

    public TestCase overrideDataSource(DataSource dataSource, Map<String, String> config) {
        return this.builder()
                .setOverrideTestDataSet(dataSource, config)
                .build();
    }

    public TestCase isChainTakeOverLastRun(boolean b) {
        return this.builder()
                .isChainTakeOverLastRun(b)
                .build();
    }

    public TestCase nestedChain(boolean nestedChain) {
        return this.builder()
                .isNestedChain(nestedChain)
                .build();
    }

    public TestCase breakNestedChain(boolean breakNestedChain) {
        return this.builder()
                .isBreakNestedChain(breakNestedChain)
                .build();
    }

    public TestCase replaceChains(TestCaseChains loaded) {
        return this.builder()
                .setChains(loaded)
                .build();
    }

    public TestCase addChain(TestCase loaded) {
        return this.builder()
                .addChain(loaded)
                .build();
    }

    public TestCase addAspect(Aspect aspect) {
        return this.builder()
                .addAspect(aspect)
                .build();
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

    public TestCase setSteps(int stepIndex, Step newStep) {
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

    public TestCase editStep(Function<ArrayList<Step>, ArrayList<Step>> converter) {
        return this.setSteps(converter.apply(this.steps));
    }

    protected TestCase setSteps(ArrayList<Step> newStep) {
        return new TestCaseBuilder(this)
                .clearStep()
                .addSteps(newStep)
                .build();
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "scriptFile=" + scriptFile +
                ", shareInput=" + shareInput +
                ", testDataSet=" + testDataSet +
                ", shareState=" + shareState +
                ", skip='" + skip + '\'' +
                ", overrideTestDataSet=" + overrideTestDataSet +
                ", lazyLoad=" + lazyLoad +
                ", nestedChain=" + nestedChain +
                ", breakNestedChain=" + breakNestedChain +
                ", chains=" + chains +
                ", aspect=" + aspect +
                ", steps=" + steps +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return isShareState() == testCase.isShareState() &&
                isNestedChain() == testCase.isNestedChain() &&
                isBreakNestedChain() == testCase.isBreakNestedChain() &&
                Objects.equal(getScriptFile(), testCase.getScriptFile()) &&
                Objects.equal(getShareInput(), testCase.getShareInput()) &&
                Objects.equal(getTestDataSet(), testCase.getTestDataSet()) &&
                Objects.equal(getSkip(), testCase.getSkip()) &&
                Objects.equal(getOverrideTestDataSet(), testCase.getOverrideTestDataSet()) &&
                Objects.equal(isLazyLoad(), testCase.isLazyLoad()) &&
                Objects.equal(getChains(), testCase.getChains()) &&
                Objects.equal(getAspect(), testCase.getAspect()) &&
                Objects.equal(steps, testCase.steps);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getScriptFile(), getShareInput(), getTestDataSet(), isShareState(), getSkip(), getOverrideTestDataSet(), isLazyLoad(), isNestedChain(), isBreakNestedChain(), getChains(), getAspect(), this.steps);
    }

}