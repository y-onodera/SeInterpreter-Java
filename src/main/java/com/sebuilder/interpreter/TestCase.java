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

import java.util.ArrayList;
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
public class TestCase extends AbstractTestRunnable<TestCase> {

    public static final String DEFAULT_SCRIPT_NAME = "New_Script";
    private final ArrayList<Step> steps;
    private final boolean closeDriver;

    public TestCase(TestCaseBuilder builder) {
        super(builder);
        this.steps = builder.getSteps();
        this.closeDriver = builder.isCloseDriver();
    }

    @Override
    public void accept(TestRunner runner, TestRunListener testRunListener) {
        runner.execute(this, testRunListener);
    }

    @Override
    public Iterable<TestRunBuilder> createTestRunBuilder() {
        return Lists.newArrayList(new TestRunBuilder(this));
    }

    @Override
    public Iterable<TestRunBuilder> createTestRunBuilder(Scenario scenario) {
        return Lists.newArrayList(new TestRunBuilder(this, scenario));
    }

    @Override
    public TestCaseBuilder builder() {
        return new TestCaseBuilder(this);
    }

    @Override
    public TestCase testCase() {
        return this;
    }

    public ArrayList<Step> steps() {
        return Lists.newArrayList(this.steps);
    }

    public boolean closeDriver() {
        return this.closeDriver;
    }

    public TestCase changeDataSourceConfig(String key, String value) {
        return this.builder()
                .addDataSourceConfig(key, value)
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

    public Suite toSuite() {
        return new SuiteBuilder(this).build();
    }

}