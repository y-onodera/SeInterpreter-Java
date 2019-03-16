package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class Suite implements Iterable<Script>, TestRunnable {

    public static final String DEFAULT_NAME = "New_Suite";

    private final TestCase testCase;

    private final ArrayList<Script> scripts = Lists.newArrayList();

    private final ScriptChain scriptChains;

    private final DataSet dataSet;

    private final boolean shareState;

    public Suite(File suiteFile
            , ArrayList<Script> aScripts
            , ScriptChain scriptChains
            , DataSource dataSource
            , Map<String, String> config
            , boolean shareState) {
        this.testCase = TestCase.of(suiteFile, DEFAULT_NAME);
        this.shareState = shareState;
        this.scripts.addAll(aScripts);
        this.scriptChains = scriptChains;
        this.dataSet = new DataSet(dataSource, config, this.getRelativePath());
    }

    @Override
    public void accept(TestRunner runner, SeInterpreterTestListener testListener) {
        runner.execute(this, testListener);
    }

    public List<TestData> loadData() {
        return this.dataSet.loadData();
    }

    public String getPath() {
        return this.testCase.path();
    }

    public String getName() {
        return this.testCase.name();
    }

    public File getRelativePath() {
        return this.testCase.relativePath();
    }

    public int scriptSize() {
        return this.scripts.size();
    }

    public int getIndex(Script script) {
        int result = -1;
        for (Script target : this.scripts) {
            result++;
            if (target.name().equals(script.name())) {
                return result;
            }
        }
        return result;
    }

    public Script get(String scriptName) {
        return this.scripts.stream()
                .filter(it -> it.name().equals(scriptName))
                .findFirst()
                .orElse(null);
    }

    public Script get(int index) {
        return this.scripts.get(index);
    }

    @Override
    public Iterator<Script> iterator() {
        return this.scripts.iterator();
    }

    public ScriptChain getScriptChains() {
        return this.scriptChains;
    }

    public boolean isShareState() {
        return this.shareState;
    }

    public Map<String, String> getDataSourceConfig() {
        return this.dataSet.getDataSourceConfig();
    }

    public DataSource getDataSource() {
        return this.dataSet.getDataSource();
    }

    public List<TestRunBuilder> getTestRuns() {
        final String suiteName = this.testCase.nameExcludeExtention();
        return this.loadData()
                .stream()
                .flatMap(it -> {
                    String rowNum = it.rowNumber();
                    TestData newRow = it.clearRowNumber();
                    final String prefix;
                    if (rowNum != null) {
                        prefix = suiteName + "_" + rowNum;
                    } else {
                        prefix = suiteName;
                    }
                    List<Script> loadedScripts = Lists.newArrayList();
                    ScriptChain loadedScriptChains = this.scriptChains;
                    for (Script script : this.scripts) {
                        Script loaded = script.loadContents(newRow);
                        loadedScripts.add(loaded);
                        if (script != loaded) {
                            loadedScriptChains = loadedScriptChains.replace(script, loaded);
                        }
                    }
                    final ScriptChain runScriptChain = loadedScriptChains;
                    return loadedScripts
                            .stream()
                            .filter(script -> !runScriptChain.containsValue(script))
                            .filter(script -> !script.skipRunning(newRow))
                            .map(script -> new TestRunBuilder(script)
                                    .addChain(runScriptChain)
                                    .addTestRunNamePrefix(prefix + "_")
                                    .setShareInput(newRow)
                            );
                })
                .collect(Collectors.toList());
    }

    public SuiteBuilder builder() {
        return new SuiteBuilder(this);
    }

    public Suite skip(String skip) {
        return builder()
                .skip(skip)
                .createSuite();
    }

    public Suite insert(Script aScript, Script newScript) {
        return builder().insertScript(aScript, newScript)
                .createSuite();
    }

    public Suite add(Script aScript, Script newScript) {
        return builder().addScript(aScript, newScript)
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
        return this.replace(aScript.name(), aScript);
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
                chain.put(this.getScriptJson(s));
            } else if (this.scriptChains.containsKey(s) && this.scriptChains.containsValue(s)) {
                chain.put(this.getScriptJson(s));
            } else if (!this.scriptChains.containsKey(s) && this.scriptChains.containsValue(s)) {
                chain.put(this.getScriptJson(s));
                JSONObject scriptPaths = new JSONObject();
                scriptPaths.put("chain", chain);
                scriptsA.put(scriptPaths);
            } else {
                scriptsA.put(this.getScriptJson(s));
            }
        }
        JSONObject data = this.dataSet.toJSON();
        if (data != null) {
            o.put("data", data);
        }
        o.put("type", "suite");
        o.put("scripts", scriptsA);
        o.put("shareState", this.shareState);
        return o;
    }

    private JSONObject getScriptJson(Script s) throws JSONException {
        JSONObject scriptPath = new JSONObject();
        if (s.isLazyLoad()) {
            scriptPath.put("lazyLoad", s.name());
        } else {
            scriptPath.put("path", this.testCase.relativePath(s));
        }
        if (!Objects.equals(s.skip(), "false")) {
            scriptPath.put("skip", s.skip());
        }
        if (s.overrideDataSource() != null) {
            JSONObject data = new JSONObject();
            final String sourceName = s.overrideDataSource().name();
            data.put("source", sourceName);
            JSONObject configs = new JSONObject();
            configs.put(sourceName, s.overrideDataSourceConfig());
            data.put("configs", configs);
            scriptPath.put("data", data);
        }
        return scriptPath;
    }

}
