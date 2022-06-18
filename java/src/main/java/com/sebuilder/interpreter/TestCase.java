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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
public record TestCase(ScriptFile scriptFile,
                       ArrayList<Step> steps,
                       DataSourceLoader dataSourceLoader,
                       Aspect aspect,
                       boolean shareState,
                       InputData shareInput,
                       DataSourceLoader overrideDataSourceLoader,
                       String skip,
                       TestCaseChains chains,
                       boolean nestedChain,
                       boolean breakNestedChain,
                       boolean preventContextAspect,
                       Function<TestCase, TestCase> lazyLoad) {

    public TestCase(TestCaseBuilder builder) {
        this(builder.getScriptFile(),
                builder.getSteps(),
                builder.getTestDataSet(),
                builder.getAspect(),
                builder.isShareState(),
                builder.getShareInput(),
                builder.getOverrideTestDataSet(),
                builder.getSkip(),
                builder.getChains(),
                builder.isNestedChain(),
                builder.isBreakNestedChain(),
                builder.isPreventContextAspect(),
                builder.getLazyLoad()
        );
    }

    public boolean run(TestRunner runner, TestRunListener testRunListener) {
        if (this.skipRunning()) {
            return true;
        }
        final TestCase materialized = this.materialized();
        boolean success = true;
        try {
            for (InputData data : materialized.loadData()) {
                TestRunner.STATUS result = runner.execute(new TestRunBuilder(materialized), data, testRunListener);
                if (result == TestRunner.STATUS.STOPPED) {
                    return false;
                }
                success = success && result == TestRunner.STATUS.SUCCESS;
            }
        } catch (IOException e) {
            testRunListener.reportError(materialized.name(), e);
            throw new AssertionError(e);
        }
        return success;
    }

    public File relativePath() {
        return this.scriptFile().relativePath();
    }

    public String name() {
        return this.scriptFile.name();
    }

    public String path() {
        return this.scriptFile.path();
    }

    public String fileName() {
        return this.scriptFile().relativize(this);
    }

    public ArrayList<Step> steps() {
        return Lists.newArrayList(this.steps);
    }

    public boolean hasChain() {
        return this.chains().size() > 0;
    }

    public boolean isLazyLoad() {
        return this.lazyLoad() != null;
    }

    public List<InputData> loadData() throws IOException {
        return this.runtimeDataSet().loadData();
    }

    public DataSourceLoader runtimeDataSet() {
        if (this.overrideDataSourceLoader().dataSource() != DataSource.NONE) {
            return this.overrideDataSourceLoader().shareInput(this.shareInput());
        }
        return this.dataSourceLoader().shareInput(this.shareInput());
    }

    public boolean include(TestCase target) {
        return this.flattenTestCases().anyMatch(it -> it.equals(target));
    }

    public Stream<TestCase> flattenTestCases() {
        return Stream.concat(Stream.of(this), this.chains.flattenTestCases());
    }

    public Suite toSuite() {
        if (this.scriptFile().type() == ScriptFile.Type.SUITE) {
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

    boolean skipRunning() {
        return this.shareInput().evaluate(this.skip);
    }

    TestCase materialized() {
        return this.changeWhenConditionMatch(TestCase::isLazyLoad, TestCase::execLazyLoad);
    }

    TestCase execLazyLoad() {
        return this.lazyLoad().apply(this);
    }

}