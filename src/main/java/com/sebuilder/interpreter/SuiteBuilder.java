package com.sebuilder.interpreter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SuiteBuilder {
    private File suiteFile;
    private final ArrayList<Script> scripts;
    private final Map<Script, Script> scriptChains;
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;
    private boolean shareState = true;

    public SuiteBuilder(Script script) {
        this(Lists.newArrayList(script));
    }

    public SuiteBuilder(ArrayList<Script> aScripts) {
        this.suiteFile = null;
        this.scripts = aScripts;
        this.scriptChains = Maps.newHashMap();
        this.dataSource = null;
        this.dataSourceConfig = Maps.newHashMap();
    }


    public SuiteBuilder(File suiteFile) {
        this.suiteFile = suiteFile;
        this.scripts = new ArrayList<>();
        this.scriptChains = Maps.newHashMap();
        this.dataSource = null;
        this.dataSourceConfig = Maps.newHashMap();
    }

    public SuiteBuilder(Suite suite) {
        if (!Strings.isNullOrEmpty(suite.getPath())) {
            this.suiteFile = new File(suite.getPath());
        } else {
            this.suiteFile = null;
        }
        this.scripts = Lists.newArrayList(suite);
        this.scriptChains = suite.getScriptChains();
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

    public SuiteBuilder addScripts(Suite s) {
        this.scripts.addAll(Lists.newArrayList(s));
        return this;
    }

    public SuiteBuilder insertScript(Script aScript, Script newScript) {
        final int index = this.scripts.indexOf(aScript);
        return addScript(newScript, index);
    }

    public SuiteBuilder addScript(Script aScript, Script newScript) {
        final int index = this.scripts.indexOf(aScript) + 1;
        return addScript(newScript, index);
    }

    public SuiteBuilder addScript(Script s) {
        this.scripts.add(s);
        return this;
    }

    public SuiteBuilder deleteScript(Script aScript) {
        this.scripts.remove(aScript);
        return this;
    }

    public SuiteBuilder addScriptChain(Script from, Script to) {
        this.scriptChains.put(from, to);
        return this;
    }

    public SuiteBuilder replace(String oldName, Script aScript) {
        final Map<Script, Script> copyScriptChains = Maps.newHashMap(this.scriptChains);
        ArrayList newScripts = Lists.newArrayList();
        for (Script script : this.scripts) {
            Script newScript = script;
            if (script.name().equals(oldName)) {
                newScript = aScript;
            }
            newScripts.add(newScript);
            this.replaceChainMap(copyScriptChains, script, newScript);
        }
        this.scripts.clear();
        this.scripts.addAll(newScripts);
        this.scriptChains.clear();
        this.scriptChains.putAll(copyScriptChains);
        return this;
    }

    public Suite createSuite() {
        final Map<Script, Script> copyScriptChains = Maps.newHashMap(this.scriptChains);
        ArrayList<Script> copyScripts = Lists.newArrayList();
        Map<String, Integer> duplicate = Maps.newHashMap();
        for (Script script : this.scripts) {
            Script copy;
            if (this.shareState) {
                copy = script.usePreviousDriverAndVars(this.shareState);
            } else {
                copy = script.copy();
            }
            final String scriptName = copy.name();
            if (duplicate.containsKey(copy.path())) {
                Optional<String> entries = copyScripts
                        .stream()
                        .map(it -> it.name())
                        .filter(it -> scriptName.startsWith(it))
                        .findFirst();
                if (entries.isPresent()) {
                    int nextCount = duplicate.get(copy.path()) + 1;
                    duplicate.put(copy.path(), nextCount);
                    copy = copy.rename(entries.get() + String.format("(%d)", nextCount));
                }
            } else {
                duplicate.put(copy.path(), 0);
            }
            copyScripts.add(copy);
            replaceChainMap(copyScriptChains, script, copy);
        }
        return new Suite(this.suiteFile
                , copyScripts
                , copyScriptChains
                , this.dataSource
                , Maps.newHashMap(this.dataSourceConfig)
                , this.shareState);
    }

    private SuiteBuilder addScript(Script newScript, int index) {
        this.scripts.add(index, newScript);
        return this;
    }

    private void replaceChainMap(Map<Script, Script> chainMap, Script oldScript, Script newScript) {
        if (chainMap.containsKey(oldScript) || chainMap.containsValue(oldScript)) {
            for (Map.Entry<Script, Script> entry : Maps.newHashMap(chainMap).entrySet()) {
                if (entry.getKey() == oldScript) {
                    chainMap.remove(oldScript);
                    chainMap.put(newScript, entry.getValue());
                } else if (entry.getValue() == oldScript) {
                    chainMap.put(entry.getKey(), newScript);
                }
            }
        }
    }
}