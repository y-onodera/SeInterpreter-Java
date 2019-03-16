package com.sebuilder.interpreter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class SuiteBuilder {
    private File suiteFile;
    private Scenario scenario;
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;
    private boolean shareState = true;
    private Function<TestCase, TestCase> converter = script -> {
        if (script.usePreviousDriverAndVars() != this.shareState) {
            return script.usePreviousDriverAndVars(this.shareState);
        }
        return script;
    };

    public SuiteBuilder(TestCase testCase) {
        this(Lists.newArrayList(testCase));
    }

    public SuiteBuilder(ArrayList<TestCase> aTestCases) {
        this.suiteFile = null;
        this.scenario = new Scenario(aTestCases);
        this.dataSource = null;
        this.dataSourceConfig = Maps.newHashMap();
    }


    public SuiteBuilder(File suiteFile) {
        this.suiteFile = suiteFile;
        this.scenario = new Scenario();
        this.dataSource = null;
        this.dataSourceConfig = Maps.newHashMap();
    }

    public SuiteBuilder(Suite suite) {
        if (!Strings.isNullOrEmpty(suite.getPath())) {
            this.suiteFile = new File(suite.getPath());
        } else {
            this.suiteFile = null;
        }
        this.scenario = suite.getScenario();
        this.dataSource = suite.getDataSource();
        this.dataSourceConfig = suite.getDataSourceConfig();
        this.shareState = suite.isShareState();
    }

    public SuiteBuilder associateWith(File target) {
        this.suiteFile = target;
        return this;
    }

    public File getSuiteFile() {
        return this.suiteFile;
    }

    public SuiteBuilder setDataSource(DataSource dataSource, HashMap<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
        return this;
    }

    public SuiteBuilder setShareState(boolean shareState) {
        this.shareState = shareState;
        return this;
    }

    public SuiteBuilder addTests(Suite s) {
        this.scenario = this.scenario.append(s);
        return this;
    }

    public SuiteBuilder insertTest(TestCase aTestCase, TestCase newTestCase) {
        final int index = this.scenario.indexOf(aTestCase);
        return addTest(newTestCase, index);
    }

    public SuiteBuilder addTest(TestCase aTestCase, TestCase newTestCase) {
        final int index = this.scenario.indexOf(aTestCase) + 1;
        return addTest(newTestCase, index);
    }

    public SuiteBuilder addTest(TestCase s) {
        this.scenario = this.scenario.append(s);
        return this;
    }

    public SuiteBuilder removeTest(TestCase aTestCase) {
        this.scenario = this.scenario.minus(aTestCase);
        return this;
    }

    public SuiteBuilder testChain(TestCase from, TestCase to) {
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

    public Suite createSuite() {
        return new Suite(this.suiteFile
                , this.scenario.map(this.converter)
                , this.dataSource
                , Maps.newHashMap(this.dataSourceConfig)
                , this.shareState);
    }

    private SuiteBuilder addTest(TestCase newTestCase, int index) {
        this.scenario = this.scenario.append(index, newTestCase);
        return this;
    }

}