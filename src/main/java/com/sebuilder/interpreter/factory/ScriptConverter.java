package com.sebuilder.interpreter.factory;

import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public class ScriptConverter {

    public String toString(Suite target) {
        try {
            JSONObject o = new JSONObject();
            JSONObject data = toJson(target.getDataSet());
            if (data != null) {
                o.put("data", data);
            }
            o.put("type", "suite");
            o.put("scripts", toJsonArray(target, target.getScenario()));
            o.put("shareState", target.isShareState());
            return o.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public String toString(TestCase target) {
        try {
            JSONObject o = new JSONObject();
            JSONArray stepsA = new JSONArray();
            for (Step s : target.steps()) {
                stepsA.put(this.toJSON(s));
            }
            o.put("steps", stepsA);
            JSONObject data = this.toJson(target.getDataSet());
            if (data != null) {
                o.put("data", data);
            }
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

    private JSONObject toJson(DataSet target) throws JSONException {
        final DataSource dataSource = target.getDataSource();
        final Map<String, String> dataSourceConfig = target.getDataSourceConfig();
        if (dataSource != null) {
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

    private JSONArray toJsonArray(Suite target, Scenario scenario) throws JSONException {
        JSONArray scriptsA = new JSONArray();
        JSONArray chain = null;
        for (TestCase s : scenario) {
            if (scenario.hasChain(s) && !scenario.isChainTarget(s)) {
                chain = new JSONArray();
                chain.put(this.getJSON(s, target));
            } else if (scenario.hasChain(s) && scenario.isChainTarget(s)) {
                chain.put(this.getJSON(s, target));
            } else if (!scenario.hasChain(s) && scenario.isChainTarget(s)) {
                chain.put(this.getJSON(s, target));
                JSONObject scriptPaths = new JSONObject();
                scriptPaths.put("chain", chain);
                scriptsA.put(scriptPaths);
            } else {
                scriptsA.put(this.getJSON(s, target));
            }
        }
        return scriptsA;
    }

    private JSONObject getJSON(TestCase testCase, Suite target) throws JSONException {
        JSONObject scriptPath = new JSONObject();
        if (testCase.isLazyLoad()) {
            scriptPath.put("lazyLoad", testCase.name());
        } else {
            scriptPath.put("path", target.getScriptFile().relativePath(testCase));
        }
        if (!Objects.equals(testCase.skip(), "false")) {
            scriptPath.put("skip", testCase.skip());
        }
        JSONObject data = this.toJson(testCase.getOverrideDataSet());
        if (data != null) {
            scriptPath.put("data", data);
        }
        return scriptPath;
    }
}
