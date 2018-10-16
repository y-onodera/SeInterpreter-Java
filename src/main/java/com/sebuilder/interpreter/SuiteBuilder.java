package com.sebuilder.interpreter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class SuiteBuilder {
    private final File suiteFile;
    private final ArrayList<Script> scripts;
    private final Map<Script, Script> scriptChains;
    private boolean shareState = true;

    public SuiteBuilder(Script script) {
        this(Lists.newArrayList(script));
    }

    public SuiteBuilder(ArrayList<Script> aScripts) {
        this.suiteFile = null;
        this.scripts = aScripts;
        this.scriptChains = Maps.newHashMap();
    }


    public SuiteBuilder(File suiteFile) {
        this.suiteFile = suiteFile;
        this.scripts = new ArrayList<>();
        this.scriptChains = Maps.newHashMap();
    }

    public SuiteBuilder(Suite suite) {
        if (!Strings.isNullOrEmpty(suite.getPath())) {
            this.suiteFile = new File(suite.getPath());
        } else {
            this.suiteFile = null;
        }
        this.scripts = Lists.newArrayList(suite);
        this.scriptChains = suite.getScriptChains();
        this.shareState = suite.isShareState();
    }

    public File getSuiteFile() {
        return this.suiteFile;
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
        this.scripts.add(this.scripts.indexOf(aScript), newScript);
        return this;
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
            if (script.name.equals(oldName)) {
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
        for (Script script : this.scripts) {
            final Script copy;
            if (this.shareState) {
                copy = script.reusePreviousDriverAndVars();
            } else {
                copy = script.copy();
            }
            copyScripts.add(copy);
            replaceChainMap(copyScriptChains, script, copy);
        }
        return new Suite(this.suiteFile, copyScripts, copyScriptChains, this.shareState);
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