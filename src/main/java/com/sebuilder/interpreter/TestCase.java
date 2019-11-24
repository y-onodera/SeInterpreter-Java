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
import java.util.function.Function;
import java.util.function.Predicate;

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
    private final ArrayList<Step> steps;
    private final TestDataSet testDataSet;
    private final String skip;
    private final TestDataSet overrideTestDataSet;
    private final Function<TestCase, TestCase> lazyLoad;
    private final boolean nestedChain;
    private final boolean breakNestedChain;
    private final TestCaseChains chains;
    private final Aspect aspect;
    private final boolean shareState;
    private final TestData shareInput;

    public TestCase(TestCaseBuilder builder) {
        this.scriptFile = builder.getScriptFile();
        this.steps = builder.getSteps();
        this.testDataSet = builder.getTestDataSet();
        this.skip = builder.getSkip();
        this.overrideTestDataSet = builder.getOverrideTestDataSet();
        this.lazyLoad = builder.getLazyLoad();
        this.nestedChain = builder.isNestedChain();
        this.breakNestedChain = builder.isBreakNestedChain();
        this.chains = builder.getChains();
        this.aspect = builder.getAspect();
        this.shareState = builder.isShareState();
        this.shareInput = builder.getShareInput();
    }

    public boolean run(TestRunner runner, TestRunListener testRunListener) {
        if (this.skipRunning(this.getShareInput())) {
            return true;
        }
        final TestCase materialized = this.materialized();
        for (TestData data : materialized.loadData()) {
            TestRunner.STATUS result = runner.execute(new TestRunBuilder(materialized), data, testRunListener);
            if (result == TestRunner.STATUS.STOPPED) {
                return false;
            }
        }
        return true;
    }

    public Suite toSuite() {
        if (this.getScriptFile().type() == ScriptFile.Type.SUITE) {
            return new Suite(this);
        }
        return new Suite(TestCaseBuilder.suite(null).addChain(this).build());
    }

    public ScriptFile getScriptFile() {
        return scriptFile;
    }

    public File relativePath() {
        return this.getScriptFile().relativePath();
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

    public ArrayList<Step> steps() {
        return Lists.newArrayList(this.steps);
    }

    public TestDataSet getTestDataSet() {
        return testDataSet;
    }

    public String getSkip() {
        return skip;
    }

    public TestDataSet getOverrideTestDataSet() {
        return overrideTestDataSet;
    }

    public boolean isLazyLoad() {
        return this.getLazyLoad() != null;
    }

    public Function<TestCase, TestCase> getLazyLoad() {
        return lazyLoad;
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

    public boolean isShareState() {
        return this.shareState;
    }

    public TestData getShareInput() {
        return this.shareInput;
    }

    public boolean skipRunning(TestData param) {
        return param.evaluate(this.skip);
    }

    public TestCaseBuilder builder() {
        return new TestCaseBuilder(this);
    }

    public TestCase map(Function<TestCaseBuilder, TestCaseBuilder> function) {
        return function.apply(this.builder()).build();
    }

    public TestCase changeWhenConditionMatch(Predicate<TestCase> condition, Function<TestCase, TestCase> function) {
        if (condition.test(this)) {
            return function.apply(this);
        }
        return this;
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
        return this.map(it -> it.clearStep().addSteps(converter.apply(this.steps)));
    }

    protected TestCase materialized() {
        return this.changeWhenConditionMatch(TestCase::isLazyLoad
                , matches -> matches.lazyLoad());
    }

    protected TestCase lazyLoad() {
        return this.getLazyLoad().apply(this);
    }

    protected List<TestData> loadData() {
        if (this.getOverrideTestDataSet().getDataSource() != null) {
            return this.getOverrideTestDataSet().loadData(this.getShareInput());
        }
        return this.getTestDataSet().loadData(this.getShareInput());
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "scriptFile=" + scriptFile +
                ", steps=" + steps +
                ", testDataSet=" + testDataSet +
                ", skip='" + skip + '\'' +
                ", overrideTestDataSet=" + overrideTestDataSet +
                ", lazyLoad=" + lazyLoad +
                ", nestedChain=" + nestedChain +
                ", breakNestedChain=" + breakNestedChain +
                ", chains=" + chains +
                ", aspect=" + aspect +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCase testCase = (TestCase) o;
        return isNestedChain() == testCase.isNestedChain() &&
                isBreakNestedChain() == testCase.isBreakNestedChain() &&
                Objects.equal(getScriptFile(), testCase.getScriptFile()) &&
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
        return Objects.hashCode(getScriptFile(), getTestDataSet(), getSkip(), getOverrideTestDataSet(), isLazyLoad(), isNestedChain(), isBreakNestedChain(), getChains(), getAspect(), this.steps);
    }

}