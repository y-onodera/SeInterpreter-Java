package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Suite implements Iterable<Script> {

    private String name;

    private String path;

    private ArrayList<Script> scripts = Lists.newArrayList();

    private Map<Script, Script> scriptChains = Maps.newHashMap();

    private DataSource dataSource;

    private Map<String, String> dataSourceConfig;

    private final boolean shareState;

    public Suite(File suiteFile, ArrayList<Script> aScripts, Map<Script, Script> scriptChains, boolean shareState) {
        if (suiteFile != null) {
            this.name = suiteFile.getName();
            this.path = suiteFile.getAbsolutePath();
        } else {
            this.name = "New_Suite";
            this.path = null;
        }
        this.shareState = shareState;
        this.scripts.addAll(aScripts);
        this.scriptChains.putAll(scriptChains);
    }

    public void setDataSource(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
    }

    public List<Map<String, String>> loadData() {
        if (this.dataSource == null) {
            return Lists.newArrayList(new HashMap<>());
        }
        return this.dataSource.getData(this.dataSourceConfig, null);
    }

    public String getPath() {
        return path;
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

    public Map<Script, Script> getScriptChains() {
        return Maps.newHashMap(scriptChains);
    }

    public boolean isShareState() {
        return shareState;
    }

    public List<TestRunBuilder> getTestRuns() {
        return this.scripts
                .stream()
                .filter(it -> !scriptChains.containsValue(it))
                .map(it -> new TestRunBuilder(it).addChain(this.scriptChains))
                .collect(Collectors.toList());
    }

    public Suite add(Script aScript) {
        return new SuiteBuilder(this)
                .addScript(aScript)
                .createSuite();
    }

    public Suite replace(Script aScript) {
        return this.replace(aScript.name, aScript);
    }

    public Suite replace(String oldName, Script newValue) {
        return new SuiteBuilder(this)
                .replace(oldName, newValue)
                .createSuite();
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
