package com.sebuilder.interpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ScriptBuilder {
    private ArrayList<Step> steps;
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
        this.path = "scriptPath";
        this.name = "scriptName";
        this.relativePath = null;
        this.usePreviousDriverAndVars = false;
        this.closeDriver = true;
        this.dataSource = null;
        this.dataSourceConfig = null;
        this.overrideDataSource = null;
        this.overrideDataSourceConfig = null;
        this.skip = "false";
    }

    public ScriptBuilder(Script currentDisplay) {
        this.steps = new ArrayList<>(currentDisplay.steps());
        this.path = currentDisplay.path();
        this.name = currentDisplay.name();
        this.relativePath = currentDisplay.relativePath();
        this.usePreviousDriverAndVars = currentDisplay.usePreviousDriverAndVars();
        this.closeDriver = currentDisplay.closeDriver();
        this.dataSource = currentDisplay.dataSource();
        this.dataSourceConfig = currentDisplay.dataSourceConfig();
        this.overrideDataSource = currentDisplay.overrideDataSource();
        this.overrideDataSourceConfig = currentDisplay.overrideDataSourceConfig();
        this.skip = currentDisplay.skip();
    }

    public ArrayList<Step> getSteps() {
        return steps;
    }

    public String getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public File getRelativePath() {
        return relativePath;
    }

    public boolean isUsePreviousDriverAndVars() {
        return usePreviousDriverAndVars;
    }

    public boolean isCloseDriver() {
        return closeDriver;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Map<String, String> getDataSourceConfig() {
        return dataSourceConfig;
    }

    public DataSource getOverrideDataSource() {
        return overrideDataSource;
    }

    public Map<String, String> getOverrideDataSourceConfig() {
        return overrideDataSourceConfig;
    }

    public String getSkip() {
        return skip;
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

    public ScriptBuilder setDataSource(DataSource dataSource, HashMap<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
        return this;
    }

    public ScriptBuilder overrideDataSource(DataSource dataSource, HashMap<String, String> config) {
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