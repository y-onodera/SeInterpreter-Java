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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    private final DataSourceLoader dataSourceLoader;
    private final Aspect aspect;
    private final boolean shareState;
    private final InputData shareInput;
    private final DataSourceLoader overrideDataSourceLoader;
    private final String skip;
    private final TestCaseChains chains;
    private final boolean nestedChain;
    private final boolean breakNestedChain;
    private boolean preventContextAspect;
    private final BiFunction<TestCase, TestRunListener, TestCase> lazyLoad;

    public TestCase(TestCaseBuilder builder) {
        this.scriptFile = builder.getScriptFile();
        this.steps = builder.getSteps();
        this.dataSourceLoader = builder.getTestDataSet();
        this.aspect = builder.getAspect();
        this.shareState = builder.isShareState();
        this.shareInput = builder.getShareInput();
        this.overrideDataSourceLoader = builder.getOverrideTestDataSet();
        this.skip = builder.getSkip();
        this.chains = builder.getChains();
        this.nestedChain = builder.isNestedChain();
        this.breakNestedChain = builder.isBreakNestedChain();
        this.lazyLoad = builder.getLazyLoad();
        this.preventContextAspect = builder.isPreventContextAspect();
    }

    public boolean run(TestRunner runner, TestRunListener testRunListener) {
        if (this.skipRunning()) {
            return true;
        }
        final TestCase materialized = this.materialized(testRunListener);
        try {
            for (InputData data : materialized.loadData()) {
                TestRunner.STATUS result = runner.execute(new TestRunBuilder(materialized), data, testRunListener);
                if (result == TestRunner.STATUS.STOPPED) {
                    return false;
                }
            }
        } catch (IOException e) {
            testRunListener.reportError(materialized.name(), e);
            throw new AssertionError(e);
        }
        return true;
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

    public DataSourceLoader getDataSourceLoader() {
        return dataSourceLoader;
    }

    public Aspect getAspect() {
        return this.aspect;
    }

    public boolean isShareState() {
        return this.shareState;
    }

    public InputData getShareInput() {
        return this.shareInput;
    }

    public DataSourceLoader getOverrideDataSourceLoader() {
        return overrideDataSourceLoader;
    }

    public String getSkip() {
        return skip;
    }

    public boolean hasChain() {
        return this.getChains().size() > 0;
    }

    public TestCaseChains getChains() {
        return this.chains;
    }

    public boolean isNestedChain() {
        return this.nestedChain;
    }

    public boolean isBreakNestedChain() {
        return this.breakNestedChain;
    }

    public boolean isPreventContextAspect() {
        return this.preventContextAspect;
    }

    public boolean isLazyLoad() {
        return this.getLazyLoad() != null;
    }

    public BiFunction<TestCase, TestRunListener, TestCase> getLazyLoad() {
        return lazyLoad;
    }

    public List<InputData> loadData() throws IOException {
        return this.runtimeDataSet().loadData();
    }

    public DataSourceLoader runtimeDataSet() {
        if (this.getOverrideDataSourceLoader().getDataSource() != DataSource.NONE) {
            return this.getOverrideDataSourceLoader().shareInput(this.getShareInput());
        }
        return this.getDataSourceLoader().shareInput(this.getShareInput());
    }

    public boolean include(TestCase target) {
        return this.flattenTestCases().anyMatch(it -> it.equals(target));
    }

    public Stream<TestCase> flattenTestCases() {
        return Stream.concat(Stream.of(this), this.chains.flattenTestCases());
    }

    public Suite toSuite() {
        if (this.getScriptFile().type() == ScriptFile.Type.SUITE) {
            return new Suite(this);
        }
        return new Suite(TestCaseBuilder.suite(null).addChain(this).build());
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

    protected boolean skipRunning() {
        return this.getShareInput().evaluate(this.skip);
    }

    protected TestCase materialized(TestRunListener testRunListener) {
        return this.changeWhenConditionMatch(TestCase::isLazyLoad, it -> it.lazyLoad(testRunListener));
    }

    protected TestCase lazyLoad(TestRunListener testRunListener) {
        return this.getLazyLoad().apply(this, testRunListener);
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "scriptFile=" + scriptFile +
                ", steps=" + steps +
                ", dataSourceLoader=" + dataSourceLoader +
                ", aspect=" + aspect +
                ", shareState=" + shareState +
                ", shareInput=" + shareInput +
                ", overrideDataSourceLoader=" + overrideDataSourceLoader +
                ", skip='" + skip + '\'' +
                ", chains=" + chains +
                ", nestedChain=" + nestedChain +
                ", breakNestedChain=" + breakNestedChain +
                ", preventContextAspect=" + preventContextAspect +
                ", lazyLoad=" + lazyLoad +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestCase)) return false;
        TestCase testCase = (TestCase) o;
        return shareState == testCase.shareState
                && nestedChain == testCase.nestedChain
                && breakNestedChain == testCase.breakNestedChain
                && preventContextAspect == testCase.preventContextAspect
                && Objects.equal(scriptFile, testCase.scriptFile)
                && Objects.equal(steps, testCase.steps)
                && Objects.equal(dataSourceLoader, testCase.dataSourceLoader)
                && Objects.equal(aspect, testCase.aspect)
                && Objects.equal(shareInput, testCase.shareInput)
                && Objects.equal(overrideDataSourceLoader, testCase.overrideDataSourceLoader)
                && Objects.equal(skip, testCase.skip)
                && Objects.equal(chains, testCase.chains)
                && Objects.equal(lazyLoad, testCase.lazyLoad)
                ;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.scriptFile
                , this.steps
                , this.dataSourceLoader
                , this.aspect
                , this.shareState
                , this.shareInput
                , this.overrideDataSourceLoader
                , this.skip
                , this.chains
                , this.nestedChain
                , this.breakNestedChain
                , this.preventContextAspect
                , this.lazyLoad
        );
    }
}