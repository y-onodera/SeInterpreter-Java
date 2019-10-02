package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Function;

public class SuiteBuilder extends AbstractTestRunnable.AbstractBuilder<Suite, SuiteBuilder> {
    private Scenario scenario;
    private Function<TestCase, TestCase> converter = script -> {
        if (script.isShareState() != this.isShareState()) {
            return script.shareState(this.isShareState());
        }
        return script;
    };

    public SuiteBuilder(Suite suite) {
        super(suite);
        this.scenario = suite.getScenario();
    }

    public SuiteBuilder(TestCase testCase) {
        this(Lists.newArrayList(testCase));
    }

    public SuiteBuilder(ArrayList<TestCase> aTestCases) {
        this(new ScriptFile(Suite.DEFAULT_NAME), new Scenario(aTestCases));
        this.isShareState(true);
    }

    public SuiteBuilder(File suiteFile) {
        this(ScriptFile.of(suiteFile, Suite.DEFAULT_NAME), new Scenario());
    }

    private SuiteBuilder(ScriptFile scriptFile, Scenario scenario) {
        super(scriptFile);
        this.scenario = scenario;
        this.setDataSource(null, Maps.newHashMap());
        this.setSkip("false");
    }

    @Override
    public Suite build() {
        return new Suite(this);
    }

    @Override
    public SuiteBuilder associateWith(File target) {
        return this.setScriptFile(ScriptFile.of(target, this.getScriptFile().name()));
    }

    public Scenario getScenario() {
        return this.scenario.map(this.converter);
    }

    public SuiteBuilder addTests(Suite s) {
        this.scenario = this.scenario.append(s);
        return this;
    }

    public SuiteBuilder addTest(TestCase s) {
        this.scenario = this.scenario.append(s);
        return this;
    }

    public SuiteBuilder addTest(TestCase aTestCase, TestCase newTestCase) {
        final int index = this.scenario.indexOf(aTestCase) + 1;
        return addTest(newTestCase, index);
    }

    public SuiteBuilder insertTest(TestCase aTestCase, TestCase newTestCase) {
        final int index = this.scenario.indexOf(aTestCase);
        return addTest(newTestCase, index);
    }

    public SuiteBuilder removeTest(TestCase aTestCase) {
        this.scenario = this.scenario.minus(aTestCase);
        return this;
    }

    public SuiteBuilder chain(TestCase from, TestCase to) {
        this.scenario = this.scenario.appendNewChain(from, to);
        return this;
    }

    public SuiteBuilder replace(String oldName, TestCase aTestCase) {
        this.scenario = this.scenario.replaceTest(oldName, aTestCase);
        return this;
    }

    public SuiteBuilder skip(String skip) {
        this.converter = this.converter.andThen(it -> it.skip(skip));
        return this;
    }

    public SuiteBuilder setAspect(Aspect aspect) {
        this.scenario = this.scenario.addAspect(aspect);
        return this;
    }

    @Override
    protected SuiteBuilder self() {
        return this;
    }

    private SuiteBuilder addTest(TestCase newTestCase, int index) {
        this.scenario = this.scenario.append(index, newTestCase);
        return this;
    }
}