package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public class SebuilderToStringConverter implements TestCaseConverter{

    @Override
    public String toString(Suite target) {
        return toStringSuite(target.head());
    }

    @Override
    public String toString(TestCase target) {
        if (target.scriptFile().type() == ScriptFile.Type.SUITE) {
            return toStringSuite(target);
        }
        return toStringTest(target);
    }

    protected String toStringTest(TestCase target) {
        try {
            JSONObject o = new JSONObject();
            JSONArray stepsA = new JSONArray();
            for (Step s : target.steps()) {
                stepsA.put(this.toJSON(s));
            }
            o.put("steps", stepsA);
            JSONObject data = this.toJson(target.dataSourceLoader());
            if (data != null) {
                o.put("data", data);
            }
            return o.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected String toStringSuite(TestCase target) {
        try {
            JSONObject o = new JSONObject();
            JSONObject data = toJson(target.dataSourceLoader());
            if (data != null) {
                o.put("data", data);
            }
            o.put("type", "suite");
            o.put("scripts", this.toJsonArray(target));
            o.put("shareState", target.shareState());
            return o.toString(4);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    protected JSONObject toJSON(Step s) throws JSONException {
        JSONObject o = new JSONObject();
        if (s.name() != null) {
            o.put("step_name", s.name());
        }
        o.put("type", s.type().getStepTypeName());
        o.put("negated", s.negated());
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

    protected JSONObject toJSON(Locator locator) throws JSONException {
        JSONObject o = new JSONObject();
        o.put("type", locator.type());
        o.put("value", locator.value());
        return o;
    }

    protected JSONObject toJson(DataSourceLoader target) throws JSONException {
        final DataSource dataSource = target.dataSource();
        final Map<String, String> dataSourceConfig = target.dataSourceConfig();
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

    protected JSONArray toJsonArray(TestCase target) throws JSONException {
        JSONArray scriptsA = new JSONArray();
        for (TestCase s : target.chains()) {
            if (s.chains().size() > 0 && s.scriptFile().type() == ScriptFile.Type.TEST) {
                JSONObject scriptPaths = this.chainToJson(target.scriptFile(), s);
                scriptsA.put(scriptPaths);
            } else {
                scriptsA.put(this.toJson(s, target.scriptFile()));
            }
        }
        return scriptsA;
    }

    protected JSONObject chainToJson(ScriptFile suiteFile, TestCase chainHeader) throws JSONException {
        JSONArray chain = new JSONArray();
        chain.put(this.toJson(chainHeader, suiteFile));
        this.addChain(suiteFile, chainHeader, chain);
        JSONObject scriptPaths = new JSONObject();
        scriptPaths.put("chain", chain);
        return scriptPaths;
    }

    protected void addChain(ScriptFile suiteFile, TestCase chainHeader, JSONArray addChainTo) throws JSONException {
        for (TestCase chainCase : chainHeader.chains()) {
            addChainTo.put(this.toJson(chainCase, suiteFile));
            if (chainCase.chains().size() > 0 && chainCase.scriptFile().type() == ScriptFile.Type.TEST) {
                this.addChain(suiteFile, chainCase, addChainTo);
            }
        }
    }

    protected JSONObject toJson(TestCase testCase, ScriptFile scriptFile) throws JSONException {
        JSONObject scriptPath = new JSONObject();
        if (testCase.isLazyLoad()) {
            scriptPath.put("lazyLoad", testCase.name());
        } else {
            scriptPath.put("path", scriptFile.relativize(testCase));
        }
        if (!Objects.equals(testCase.skip(), "false")) {
            scriptPath.put("skip", testCase.skip());
        }
        if (testCase.nestedChain()) {
            scriptPath.put("nestedChain", "true");
        }
        if (testCase.breakNestedChain()) {
            scriptPath.put("breakNestedChain", "true");
        }
        if (testCase.preventContextAspect()) {
            scriptPath.put("preventContextAspect", "true");
        }
        JSONObject data = this.toJson(testCase.overrideDataSourceLoader());
        if (data != null) {
            scriptPath.put("data", data);
        }
        return scriptPath;
    }
}
