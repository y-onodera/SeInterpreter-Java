package com.sebuilder.interpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class ScriptBuilder {
    private ArrayList<Step> steps;
    private Function<TestData, Script> lazyLoad;
    private String path;
    private String name;
    private File relativePath;
    private boolean usePreviousDriverAndVars;
    private boolean closeDriver;
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;
    private DataSource overrideDataSource;
    private Map<String, String> overrideDataSourceConfig;
    private String skip;

    public ScriptBuilder() {
        this.steps = new ArrayList<>();
        this.lazyLoad = null;
        this.path = "scriptPath";
        this.name = "scriptName";
        this.relativePath = null;
        this.dataSource = null;
        this.dataSourceConfig = null;
        this.usePreviousDriverAndVars = false;
        this.closeDriver = true;
        this.overrideDataSource = null;
        this.overrideDataSourceConfig = null;
        this.skip = "false";
    }

    public ScriptBuilder(Script currentDisplay) {
        this.steps = new ArrayList<>(currentDisplay.steps());
        this.lazyLoad = currentDisplay.lazyLoad();
        this.path = currentDisplay.path();
        this.name = currentDisplay.name();
        this.relativePath = currentDisplay.relativePath();
        this.dataSource = currentDisplay.dataSource();
        this.dataSourceConfig = currentDisplay.dataSourceConfig();
        this.usePreviousDriverAndVars = currentDisplay.usePreviousDriverAndVars();
        this.closeDriver = currentDisplay.closeDriver();
        this.overrideDataSource = currentDisplay.overrideDataSource();
        this.overrideDataSourceConfig = currentDisplay.overrideDataSourceConfig();
        this.skip = currentDisplay.skip();
    }

    public static Script lazyLoad(String beforeReplace, Function<TestData, Script> lazyLoad) {
        ScriptBuilder builder = new ScriptBuilder();
        builder.setName(beforeReplace);
        builder.lazyLoad = lazyLoad;
        return builder.createScript();
    }

    public ArrayList<Step> getSteps() {
        return this.steps;
    }

    public Function<TestData, Script> getLazyLoad() {
        return this.lazyLoad;
    }

    public String getPath() {
        return this.path;
    }

    public String getName() {
        return this.name;
    }

    public File getRelativePath() {
        return this.relativePath;
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

    public ScriptBuilder clearStep() {
        this.steps.clear();
        return this;
    }

    public ScriptBuilder addSteps(ArrayList<Step> steps) {
        this.steps.addAll(steps);
        return this;
    }

    public ScriptBuilder addStep(Step aStep) {
        this.steps.add(aStep);
        return this;
    }

    public ScriptBuilder associateWith(File target) {
        if (target != null) {
            this.name = target.getName();
            this.path = target.getPath();
            this.relativePath = target.getAbsoluteFile().getParentFile();
        } else {
            this.name = "New_Script";
            this.path = null;
            this.relativePath = null;
        }
        return this;
    }

    public ScriptBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public ScriptBuilder setDataSource(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
        return this;
    }

    public ScriptBuilder overrideDataSource(DataSource dataSource, Map<String, String> config) {
        this.overrideDataSource = dataSource;
        this.overrideDataSourceConfig = config;
        return this;
    }

    public ScriptBuilder setSkip(String skip) {
        this.skip = skip;
        return this;
    }

    public ScriptBuilder usePreviousDriverAndVars(boolean userPreviousDriverAndVars) {
        if (userPreviousDriverAndVars) {
            this.closeDriver = false;
            this.usePreviousDriverAndVars = true;
        } else {
            this.closeDriver = true;
            this.usePreviousDriverAndVars = false;
        }
        return this;
    }

    public Script createScript() {
        return new Script(this);
    }

}