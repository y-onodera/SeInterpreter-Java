package com.sebuilder.interpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class TestCaseBuilder {
    private ArrayList<Step> steps;
    private Function<TestData, TestCase> lazyLoad;
    private boolean usePreviousDriverAndVars;
    private boolean closeDriver;
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;
    private DataSource overrideDataSource;
    private Map<String, String> overrideDataSourceConfig;
    private String skip;
    private ScriptFile scriptFile;

    public TestCaseBuilder() {
        this.steps = new ArrayList<>();
        this.lazyLoad = null;
        this.scriptFile = new ScriptFile(TestCase.DEFAULT_SCRIPT_NAME);
        this.dataSource = null;
        this.dataSourceConfig = null;
        this.usePreviousDriverAndVars = false;
        this.closeDriver = true;
        this.overrideDataSource = null;
        this.overrideDataSourceConfig = null;
        this.skip = "false";
    }

    public TestCaseBuilder(TestCase currentDisplay) {
        this.steps = new ArrayList<>(currentDisplay.steps());
        this.lazyLoad = currentDisplay.lazyLoad();
        this.scriptFile = currentDisplay.testCase();
        this.dataSource = currentDisplay.dataSource();
        this.dataSourceConfig = currentDisplay.dataSourceConfig();
        this.usePreviousDriverAndVars = currentDisplay.usePreviousDriverAndVars();
        this.closeDriver = currentDisplay.closeDriver();
        this.overrideDataSource = currentDisplay.overrideDataSource();
        this.overrideDataSourceConfig = currentDisplay.overrideDataSourceConfig();
        this.skip = currentDisplay.skip();
    }

    public static TestCase lazyLoad(String beforeReplace, Function<TestData, TestCase> lazyLoad) {
        TestCaseBuilder builder = new TestCaseBuilder();
        builder.setName(beforeReplace);
        builder.lazyLoad = lazyLoad;
        return builder.createScript();
    }

    public ArrayList<Step> getSteps() {
        return this.steps;
    }

    public Function<TestData, TestCase> getLazyLoad() {
        return this.lazyLoad;
    }

    public ScriptFile getScriptFile() {
        return this.scriptFile;
    }

    public File getRelativePath() {
        return this.getScriptFile().relativePath();
    }

    public boolean isUsePreviousDriverAndVars() {
        return this.usePreviousDriverAndVars;
    }

    public boolean isCloseDriver() {
        return this.closeDriver;
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public Map<String, String> getDataSourceConfig() {
        return this.dataSourceConfig;
    }

    public DataSource getOverrideDataSource() {
        return this.overrideDataSource;
    }

    public Map<String, String> getOverrideDataSourceConfig() {
        return this.overrideDataSourceConfig;
    }

    public String getSkip() {
        return this.skip;
    }

    public DataSet getDataSet() {
        return new DataSet(this.getDataSource(), this.getDataSourceConfig(), this.getRelativePath());
    }

    public DataSet getOverrideDataSet() {
        return new DataSet(this.getOverrideDataSource(), this.getOverrideDataSourceConfig(), this.getRelativePath());
    }

    public TestCaseBuilder clearStep() {
        this.steps.clear();
        return this;
    }

    public TestCaseBuilder addSteps(ArrayList<Step> steps) {
        this.steps.addAll(steps);
        return this;
    }

    public TestCaseBuilder addStep(Step aStep) {
        this.steps.add(aStep);
        return this;
    }

    public TestCaseBuilder associateWith(File target) {
        this.scriptFile = ScriptFile.of(target, TestCase.DEFAULT_SCRIPT_NAME);
        return this;
    }

    public TestCaseBuilder setName(String name) {
        this.scriptFile = scriptFile.changeName(name);
        return this;
    }

    public TestCaseBuilder setDataSource(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
        return this;
    }

    public TestCaseBuilder overrideDataSource(DataSource dataSource, Map<String, String> config) {
        this.overrideDataSource = dataSource;
        this.overrideDataSourceConfig = config;
        return this;
    }

    public TestCaseBuilder setSkip(String skip) {
        this.skip = skip;
        return this;
    }

    public TestCaseBuilder usePreviousDriverAndVars(boolean userPreviousDriverAndVars) {
        if (userPreviousDriverAndVars) {
            this.closeDriver = false;
            this.usePreviousDriverAndVars = true;
        } else {
            this.closeDriver = true;
            this.usePreviousDriverAndVars = false;
        }
        return this;
    }

    public TestCase createScript() {
        return new TestCase(this);
    }

}