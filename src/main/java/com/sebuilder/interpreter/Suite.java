package com.sebuilder.interpreter;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Suite implements Iterable<Script> {

    private final String name;

    private final String path;

    private final File relativePath;

    private final ArrayList<Script> scripts = Lists.newArrayList();

    private final Map<Script, Script> scriptChains = Maps.newHashMap();

    private final DataSource dataSource;

    private final Map<String, String> dataSourceConfig;

    private final boolean shareState;

    public Suite(File suiteFile
            , ArrayList<Script> aScripts
            , Map<Script, Script> scriptChains
            , DataSource dataSource
            , Map<String, String> config
            , boolean shareState) {
        if (suiteFile != null) {
            this.name = suiteFile.getName();
            this.path = suiteFile.getAbsolutePath();
            this.relativePath = suiteFile.getParentFile().getAbsoluteFile();
        } else {
            this.name = "New_Suite";
            this.path = null;
            this.relativePath = null;
        }
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
        this.shareState = shareState;
        this.scripts.addAll(aScripts);
        this.scriptChains.putAll(scriptChains);
    }

    public List<Map<String, String>> loadData() {
        if (this.dataSource == null) {
            return Lists.newArrayList(new HashMap<>());
        }
        return this.dataSource.getData(this.dataSourceConfig, this.relativePath);
    }

    public String getPath() {
        return this.path;
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
        return Maps.newHashMap(this.scriptChains);
    }

    public boolean isShareState() {
        return this.shareState;
    }

    public Map<String, String> getDataSourceConfig() {
        return Maps.newHashMap(this.dataSourceConfig);
    }

    public DataSource getDataSource() {
        return this.dataSource;
    }

    public List<TestRunBuilder> getTestRuns() {
        final String suiteName;
        if (this.path != null && name.contains(".")) {
            suiteName = name.substring(0, name.lastIndexOf("."));
        } else {
            suiteName = this.name;
        }
        return this.loadData()
                .stream()
                .flatMap(it -> {
                    String rowNum = it.get(DataSource.ROW_NUMBER);
                    it.remove(DataSource.ROW_NUMBER);
                    final String prefix;
                    if (rowNum != null) {
                        prefix = suiteName + "_" + rowNum;
                    } else {
                        prefix = suiteName;
                    }
                    return this.scripts
                            .stream()
                            .filter(script -> !scriptChains.containsValue(script))
                            .map(script -> new TestRunBuilder(script)
                                    .addChain(this.scriptChains)
                                    .addTestRunNamePrefix(prefix + "_")
                                    .setShareInput(it));
                })
                .collect(Collectors.toList());
    }

    public SuiteBuilder builder() {
        return new SuiteBuilder(this);
    }

    public Suite insert(Script aScript, Script newScript) {
        return builder().insertScript(aScript, newScript)
                .createSuite();
    }

    public Suite add(Script aScript) {
        return builder()
                .addScript(aScript)
                .createSuite();
    }

    public Suite delete(Script aScript) {
        return builder()
                .deleteScript(aScript)
                .createSuite();
    }

    public Suite replace(Script aScript) {
        return this.replace(aScript.name, aScript);
    }

    public Suite replace(String oldName, Script newValue) {
        return builder()
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
                scriptPath.put("path", relativePath(s));
                chain.put(scriptPath);
            } else if (this.scriptChains.containsKey(s) && this.scriptChains.containsValue(s)) {
                JSONObject scriptPath = new JSONObject();
                scriptPath.put("path", relativePath(s));
                chain.put(scriptPath);
            } else if (!this.scriptChains.containsKey(s) && this.scriptChains.containsValue(s)) {
                JSONObject scriptPath = new JSONObject();
                scriptPath.put("path", relativePath(s));
                chain.put(scriptPath);
                JSONObject scriptPaths = new JSONObject();
                scriptPaths.put("paths", chain);
                scriptsA.put(scriptPaths);
            } else {
                JSONObject scriptPath = new JSONObject();
                scriptPath.put("path", relativePath(s));
                scriptsA.put(scriptPath);
            }
        }
        if (this.dataSource != null) {
            JSONObject data = new JSONObject();
            final String sourceName = this.dataSource.getClass().getSimpleName().toLowerCase();
            data.put("source", sourceName);
            JSONObject configs = new JSONObject();
            configs.put(sourceName, this.dataSourceConfig);
            data.put("configs", configs);
            o.put("data", data);
        }
        o.put("type", "suite");
        o.put("scripts", scriptsA);
        o.put("shareState", this.shareState);
        return o;
    }

    private String relativePath(Script s) {
        if (this.relativePath == null && !Strings.isNullOrEmpty(s.path)) {
            return s.path;
        } else if (Strings.isNullOrEmpty(s.path)) {
            return "script/" + s.name;
        }
        return this.relativePath.toPath().relativize(Paths.get(s.path)).toString().replace("\\", "/");
    }
}
