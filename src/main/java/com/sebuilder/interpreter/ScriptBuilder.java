package com.sebuilder.interpreter;

import com.google.common.collect.Maps;

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
    private Map<String, String> shareInputs;
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;

    public ScriptBuilder() {
        this.steps = new ArrayList<>();
        this.path = "scriptPath";
        this.name = "scriptName";
        this.relativePath = null;
        this.usePreviousDriverAndVars = false;
        this.closeDriver = true;
        this.shareInputs = Maps.newHashMap();
        this.dataSource = null;
        this.dataSourceConfig = null;
    }

    public ScriptBuilder(Script currentDisplay) {
        this.steps = new ArrayList<>(currentDisplay.steps);
        this.path = currentDisplay.path;
        this.name = currentDisplay.name;
        this.relativePath = currentDisplay.relativePath;
        this.usePreviousDriverAndVars = currentDisplay.usePreviousDriverAndVars;
        this.closeDriver = currentDisplay.closeDriver;
        this.shareInputs = currentDisplay.shareInputs;
        this.dataSource = currentDisplay.dataSource;
        this.dataSourceConfig = currentDisplay.dataSourceConfig;
    }

    public ScriptBuilder setSteps(ArrayList<Step> steps) {
        this.steps = steps;
        return this;
    }

    public ScriptBuilder associateWith(File target) {
        if (target != null) {
            this.name = target.getName();
            this.path = target.getPath();
            this.relativePath = target.getAbsoluteFile().getParentFile();
        } else {
            this.name = "System_in";
            this.path = null;
            this.relativePath = null;
        }
        return this;
    }

    public ScriptBuilder setDataSource(DataSource dataSource, HashMap<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
        return this;
    }

    public ScriptBuilder setShareInputs(Map<String, String> shareInputs) {
        this.shareInputs = shareInputs;
        return this;
    }

    public ScriptBuilder reusePreviousDriverAndVars() {
        this.closeDriver = false;
        this.usePreviousDriverAndVars = true;
        return this;
    }

    public Script createScript() {
        return new Script(steps
                , path
                , name
                , relativePath
                , usePreviousDriverAndVars
                , closeDriver
                , shareInputs
                , dataSource
                , dataSourceConfig
        );
    }
}