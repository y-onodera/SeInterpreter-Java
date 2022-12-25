package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.Objects;

public class SebuilderToStringConverter implements TestCaseConverter {

    @Override
    public String toString(final Suite target) {
        return this.toStringSuite(target.head());
    }

    @Override
    public String toString(final TestCase target) {
        if (target.scriptFile().type() == ScriptFile.Type.SUITE) {
            return this.toStringSuite(target);
        }
        return this.toStringTest(target);
    }

    protected String toStringTest(final TestCase target) {
        final JSONObject o = new JSONObject();
        final JSONArray stepsA = new JSONArray();
        target.steps().forEach(s -> stepsA.put(this.toJSON(s)));
        o.put("steps", stepsA);
        final JSONObject data = this.toJson(target.dataSourceLoader());
        if (data != null) {
            o.put("data", data);
        }
        return o.toString(4);
    }

    protected String toStringSuite(final TestCase target) {
        final JSONObject o = new JSONObject();
        final JSONObject data = this.toJson(target.dataSourceLoader());
        if (data != null) {
            o.put("data", data);
        }
        o.put("type", "suite");
        o.put("scripts", this.toJsonArray(target));
        o.put("shareState", target.shareState());
        return o.toString(4);
    }

    protected JSONObject toJSON(final Step s) {
        final JSONObject o = new JSONObject();
        if (s.name() != null) {
            o.put("step_name", s.name());
        }
        o.put("type", s.type().getStepTypeName());
        o.put("negated", s.negated());
        s.paramKeys().forEach(key -> o.put(key, s.getParam(key)));
        s.locatorKeys().forEach(key -> o.put(key, this.toJSON(s.getLocator(key))));
        if (!s.containsParam("skip")) {
            o.put("skip", "false");
        }
        return o;
    }

    protected JSONObject toJSON(final Locator locator) {
        final JSONObject o = new JSONObject();
        o.put("type", locator.type());
        o.put("value", locator.value());
        return o;
    }

    protected JSONObject toJson(final DataSourceLoader target) {
        final DataSource dataSource = target.dataSource();
        final Map<String, String> dataSourceConfig = target.dataSourceConfig();
        if (dataSource != DataSource.NONE) {
            final JSONObject data = new JSONObject();
            final String sourceName = dataSource.name();
            data.put("source", sourceName);
            final JSONObject configs = new JSONObject();
            configs.put(sourceName, dataSourceConfig);
            data.put("configs", configs);
            return data;
        }
        return null;
    }

    protected JSONArray toJsonArray(final TestCase target) {
        final JSONArray scriptsA = new JSONArray();
        target.chains().forEach(s -> {
            if (s.chains().size() > 0 && s.scriptFile().type() == ScriptFile.Type.TEST) {
                final JSONObject scriptPaths = this.chainToJson(target.scriptFile(), s);
                scriptsA.put(scriptPaths);
            } else {
                scriptsA.put(this.toJson(s, target.scriptFile()));
            }
        });
        return scriptsA;
    }

    protected JSONObject chainToJson(final ScriptFile suiteFile, final TestCase chainHeader) {
        final JSONArray chain = new JSONArray();
        chain.put(this.toJson(chainHeader, suiteFile));
        this.addChain(suiteFile, chainHeader, chain);
        final JSONObject scriptPaths = new JSONObject();
        scriptPaths.put("chain", chain);
        return scriptPaths;
    }

    protected void addChain(final ScriptFile suiteFile, final TestCase chainHeader, final JSONArray addChainTo) {
        chainHeader.chains().forEach(chainCase -> {
            addChainTo.put(this.toJson(chainCase, suiteFile));
            if (chainCase.chains().size() > 0 && chainCase.scriptFile().type() == ScriptFile.Type.TEST) {
                this.addChain(suiteFile, chainCase, addChainTo);
            }
        });
    }

    protected JSONObject toJson(final TestCase testCase, final ScriptFile scriptFile) {
        final JSONObject scriptPath = new JSONObject();
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
        final JSONObject data = this.toJson(testCase.overrideDataSourceLoader());
        if (data != null) {
            scriptPath.put("data", data);
        }
        return scriptPath;
    }
}
