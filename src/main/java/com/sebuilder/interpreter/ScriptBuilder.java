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
        this.skip = "false";
    }

    public ScriptBuilder(Script currentDisplay) {
        this.steps = new ArrayList<>(currentDisplay.steps);
        this.path = currentDisplay.path();
        this.name = currentDisplay.name();
        this.relativePath = currentDisplay.relativePath();
        this.usePreviousDriverAndVars = currentDisplay.usePreviousDriverAndVars();
        this.closeDriver = currentDisplay.closeDriver();
        this.dataSource = currentDisplay.dataSource();
        this.dataSourceConfig = currentDisplay.dataSourceConfig();
        this.skip = currentDisplay.skip();
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
        return new Script(steps
                , path
                , name
                , relativePath
                , usePreviousDriverAndVars
                , closeDriver
                , dataSource
                , dataSourceConfig
                , skip
        );
    }

}