package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Suite implements Iterable<Script> {

    private String name;

    private String path;

    private ArrayList<Script> scripts = Lists.newArrayList();

    private Map<Script, Script> scriptChains = Maps.newHashMap();

    private final boolean shareState;

    public Suite() {
        this(new ArrayList<>());
    }

    public Suite(Script script) {
        this(Lists.newArrayList(script));
    }

    public Suite(Iterable<Script> aScripts) {
        this(null, aScripts, Maps.newHashMap(), true);
    }

    public Suite(File suiteFile, Iterable<Script> aScripts, Map<Script, Script> scriptChains, boolean shareState) {
        if (suiteFile != null) {
            this.name = suiteFile.getName();
            this.path = suiteFile.getAbsolutePath();
        }
        this.shareState = shareState;
        for (Script it : aScripts) {
            scripts.add(it);
            if (shareState) {
                it.stateTakeOver();
            }
        }
        this.scriptChains.putAll(scriptChains);
    }

    public String getName() {
        return this.name;
    }

    public Script get(String scriptName) {
        return this.scripts.stream()
                .filter(it -> it.name().equals(scriptName))
                .findFirst()
                .orElse(null);
    }

    @Override
    public Iterator<Script> iterator() {
        return this.scripts.iterator();
    }

    public List<TestRunBuilder> getTestRuns() {
        return this.scripts
                .stream()
                .filter(it -> !scriptChains.containsValue(it))
                .map(it -> new TestRunBuilder(it).addChain(scriptChains))
                .collect(Collectors.toList());
    }

    public void add(Script aScript) {
        this.scripts.add(aScript);
    }

    public void replace(Script aScript) {
        ArrayList newScripts = Lists.newArrayList();
        for (Script script : this.scripts) {
            if (script.name.equals(aScript.name)) {
                newScripts.add(aScript);
            } else {
                newScripts.add(script);
            }
        }
        this.scripts.clear();
        this.scripts.addAll(newScripts);
    }

    public void replace(String oldName, Script newValue) {
        ArrayList newScripts = Lists.newArrayList();
        for (Script script : this.scripts) {
            if (script.name.equals(oldName)) {
                newScripts.add(newValue);
            } else {
                newScripts.add(script);
            }
        }
        this.scripts.clear();
        this.scripts.addAll(newScripts);
    }

    @Override
    public String toString() {
        try {
            return toJSON().toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject o = new JSONObject();
        JSONArray scriptsA = new JSONArray();
        JSONArray chain = null;
        for (Script s : this.scripts) {
            if (this.scriptChains.containsKey(s) && !this.scriptChains.containsValue(s)) {
                chain = new JSONArray();
                JSONObject scriptPath = new JSONObject();
                scriptPath.put("path", s.path);
                chain.put(scriptPath);
            } else if (this.scriptChains.containsKey(s) && this.scriptChains.containsValue(s)) {
                JSONObject scriptPath = new JSONObject();
                scriptPath.put("path", s.path);
                chain.put(scriptPath);
            } else if (!this.scriptChains.containsKey(s) && this.scriptChains.containsValue(s)) {
                JSONObject scriptPath = new JSONObject();
                scriptPath.put("path", s.path);
                chain.put(scriptPath);
                JSONObject scriptPaths = new JSONObject();
                scriptPaths.put("paths", chain);
                scriptsA.put(scriptPaths);
            } else {
                JSONObject scriptPath = new JSONObject();
                scriptPath.put("path", s.path);
                scriptsA.put(scriptPath);
            }
        }
        o.put("type", "suite");
        o.put("scripts", scriptsA);
        o.put("shareState", this.shareState);
        return o;
    }

}
