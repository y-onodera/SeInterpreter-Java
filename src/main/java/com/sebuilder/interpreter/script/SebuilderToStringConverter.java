package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public class SebuilderToStringConverter {

    protected String toString(Suite target) {
        return toStringSuite(target.head());
    }

    protected String toString(TestCase target) {
        if (target.getScriptFile().type() == ScriptFile.Type.SUITE) {
            return toStringSuite(target);
        }
        return toStringTest(target);
    }

    private String toStringTest(TestCase target) {
        try {
            JSONObject o = new JSONObject();
            JSONArray stepsA = new JSONArray();
            for (Step s : target.steps()) {
                stepsA.put(this.toJSON(s));
            }
            o.put("steps", stepsA);
            JSONObject data = this.toJson(target.getDataSourceLoader());
            if (data != null) {
                o.put("data", data);
            }
            return o.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private String toStringSuite(TestCase target) {
        try {
            JSONObject o = new JSONObject();
            JSONObject data = toJson(target.getDataSourceLoader());
            if (data != null) {
                o.put("data", data);
            }
            o.put("type", "suite");
            o.put("scripts", this.toJsonArray(target));
            o.put("shareState", target.isShareState());
            return o.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONObject toJSON(Step s) throws JSONException {
        JSONObject o = new JSONObject();
        if (s.getName() != null) {
            o.put("step_name", s.getName());
        }
        o.put("type", s.getType().getStepTypeName());
        o.put("negated", s.isNegated());
        for (String key : s.paramKeys()) {
            o.put(key, s.getParam(key));
        }
        for (String key : s.locatorKeys()) {
            o.put(key, this.toJSON(s.getLocator(key)));
        }
        if (!s.containsParam("skip")) {
            o.put("skip", "false");
        }
        return o;
    }

    private JSONObject toJSON(Locator locator) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("type", locator.type.toString());
        o.put("value", locator.value);
        return o;
    }

    private JSONObject toJson(DataSourceLoader target) throws JSONException {
        final DataSource dataSource = target.getDataSource();
        final Map<String, String> dataSourceConfig = target.getDataSourceConfig();
        if (dataSource != DataSource.NONE) {
            JSONObject data = new JSONObject();
            final String sourceName = dataSource.name();
            data.put("source", sourceName);
            JSONObject configs = new JSONObject();
            configs.put(sourceName, dataSourceConfig);
            data.put("configs", configs);
            return data;
        }
        return null;
    }

    private JSONArray toJsonArray(TestCase target) throws JSONException {
        JSONArray scriptsA = new JSONArray();
        for (TestCase s : target.getChains()) {
            if (s.getChains().size() > 0 && s.getScriptFile().type() == ScriptFile.Type.TEST) {
                JSONObject scriptPaths = this.chainToJson(target.getScriptFile(), s);
                scriptsA.put(scriptPaths);
            } else {
                scriptsA.put(this.getJSON(s, target.getScriptFile()));
            }
        }
        return scriptsA;
    }

    private JSONObject chainToJson(ScriptFile suiteFile, TestCase chainHeader) throws JSONException {
        JSONArray chain = new JSONArray();
        chain.put(this.getJSON(chainHeader, suiteFile));
        this.addChain(suiteFile, chainHeader, chain);
        JSONObject scriptPaths = new JSONObject();
        scriptPaths.put("chain", chain);
        return scriptPaths;
    }

    private void addChain(ScriptFile suiteFile, TestCase chainHeader, JSONArray addChainTo) throws JSONException {
        for (TestCase chainCase : chainHeader.getChains()) {
            addChainTo.put(this.getJSON(chainCase, suiteFile));
            if (chainCase.getChains().size() > 0 && chainCase.getScriptFile().type() == ScriptFile.Type.TEST) {
                this.addChain(suiteFile, chainCase, addChainTo);
            }
        }
    }

    private JSONObject getJSON(TestCase testCase, ScriptFile scriptFile) throws JSONException {
        JSONObject scriptPath = new JSONObject();
        if (testCase.isLazyLoad()) {
            scriptPath.put("lazyLoad", testCase.name());
        } else {
            scriptPath.put("path", scriptFile.relativize(testCase));
        }
        if (!Objects.equals(testCase.getSkip(), "false")) {
            scriptPath.put("skip", testCase.getSkip());
        }
        if (testCase.isNestedChain()) {
            scriptPath.put("nestedChain", testCase.isNestedChain());
        }
        if (testCase.isBreakNestedChain()) {
            scriptPath.put("breakNestedChain", testCase.isBreakNestedChain());
        }
        JSONObject data = this.toJson(testCase.getOverrideDataSourceLoader());
        if (data != null) {
            scriptPath.put("data", data);
        }
        return scriptPath;
    }
}
