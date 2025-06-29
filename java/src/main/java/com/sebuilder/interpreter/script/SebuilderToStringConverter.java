package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.IntStream;

public class SebuilderToStringConverter implements TestCaseConverter {

    @Override
    public String toString(final Suite target) {
        return this.toString(target.head());
    }

    @Override
    public String toString(final TestCase target) {
        return this.toJSON(target).toString(4);
    }

    @Override
    public String toString(final Aspect target) {
        final JSONObject result = new JSONObject();
        final Collection<JSONObject> results = this.toJSON(target);
        if (results.size() == 1) {
            result.put("aspect", results.iterator().next());
        } else if (!results.isEmpty()) {
            result.put("aspect", results);
        }
        return result.toString(4);
    }

    @Override
    public String toString(final Pointcut target) {
        final JSONObject result = new JSONObject();
        final Collection<JSONObject> results = this.toJSON(target);
        if (results.size() == 1) {
            result.put("pointcut", results.iterator().next());
        } else if (!results.isEmpty()) {
            result.put("pointcut", results);
        }
        return result.toString(4);
    }

    protected JSONObject toJSON(final TestCase target) {
        if (target.scriptFile().type() == ScriptFile.Type.SUITE) {
            return this.toJsonSuite(target);
        }
        return this.toJsonTest(target);
    }

    protected JSONObject toJsonTest(final TestCase target) {
        final JSONObject o = new JSONObject();
        final JSONArray stepsA = new JSONArray();
        target.steps().forEach(s -> stepsA.put(this.toJSON(s)));
        o.put("steps", stepsA);
        final JSONObject data = this.toJSON(target.dataSourceLoader());
        if (data != null) {
            o.put("data", data);
        }
        return o;
    }

    protected JSONObject toJsonSuite(final TestCase target) {
        final JSONObject o = new JSONObject();
        final JSONObject data = this.toJSON(target.dataSourceLoader());
        if (data != null) {
            o.put("data", data);
        }
        o.put("type", "suite");
        o.put("scripts", this.toJsonArray(target));
        o.put("shareState", target.shareState());
        return o;
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
        if (!s.headerParams().isEmpty()) {
            JSONArray headerParam = new JSONArray();
            s.headerParams().forEach((key, value) -> {
                JSONObject param = new JSONObject();
                param.put("key", key);
                param.put("type", value.type());
                param.put("value", value.value());
                param.put("filePath", value.filePath());
                param.put("needEncoding", value.needEncoding());
                headerParam.put(param);
            });
            o.put("httpHeader", headerParam);
        }
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

    protected JSONObject toJSON(final DataSourceLoader target) {
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
                scriptsA.put(this.toJSON(s, target.scriptFile()));
            }
        });
        return scriptsA;
    }

    protected JSONObject chainToJson(final ScriptFile suiteFile, final TestCase chainHeader) {
        final JSONArray chain = new JSONArray();
        chain.put(this.toJSON(chainHeader, suiteFile));
        this.addChain(suiteFile, chainHeader, chain);
        final JSONObject scriptPaths = new JSONObject();
        scriptPaths.put("chain", chain);
        return scriptPaths;
    }

    protected void addChain(final ScriptFile suiteFile, final TestCase chainHeader, final JSONArray addChainTo) {
        chainHeader.chains().forEach(chainCase -> {
            addChainTo.put(this.toJSON(chainCase, suiteFile));
            if (chainCase.chains().size() > 0 && chainCase.scriptFile().type() == ScriptFile.Type.TEST) {
                this.addChain(suiteFile, chainCase, addChainTo);
            }
        });
    }

    protected JSONObject toJSON(final TestCase testCase, final ScriptFile scriptFile) {
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
        final JSONObject data = this.toJSON(testCase.overrideDataSourceLoader());
        if (data != null) {
            scriptPath.put("data", data);
        }
        if (testCase.includeTestRun() != Pointcut.ANY) {
            final Collection<JSONObject> results = this.toJSON(testCase.includeTestRun());
            if (results.size() == 1) {
                scriptPath.put("include", results.iterator().next());
            } else if (!results.isEmpty()) {
                scriptPath.put("include", results);
            }
        }
        if (testCase.excludeTestRun() != Pointcut.NONE) {
            final Collection<JSONObject> results = this.toJSON(testCase.excludeTestRun());
            if (results.size() == 1) {
                scriptPath.put("exclude", results.iterator().next());
            } else if (!results.isEmpty()) {
                scriptPath.put("exclude", results);
            }
        }
        if (!testCase.aspect().equals(new Aspect())) {
            final Collection<JSONObject> results = this.toJSON(testCase.aspect());
            if (results.size() == 1) {
                scriptPath.put("aspect", results.iterator().next());
            } else if (!results.isEmpty()) {
                scriptPath.put("aspect", results);
            }
        }
        return scriptPath;
    }

    protected Collection<JSONObject> toJSON(final Aspect aspect) {
        final List<JSONObject> results = new ArrayList<>();
        for (final Interceptor interceptor : aspect) {
            if (interceptor instanceof Exportable exportable) {
                results.add(this.toJSON(exportable));
            } else if (interceptor instanceof ExtraStepExecutor extra && extra.hasStep()) {
                final JSONObject result = new JSONObject();
                result.put("pointcut", this.toJSON(extra.pointcut()));
                result.put("before", this.toJSON(extra.beforeStep()));
                result.put("after", this.toJSON(extra.afterStep()));
                result.put("failure", this.toJSON(extra.failureStep()));
                result.put("displayName", extra.displayName());
                results.add(result);
            }
        }
        return results;
    }

    protected Collection<JSONObject> toJSON(final Pointcut pointcut) {
        final List<JSONObject> results = new ArrayList<>();
        if (pointcut instanceof Pointcut.Or(Pointcut origin, Pointcut other)) {
            results.addAll(this.toJSON(origin));
            results.addAll(this.toJSON(other));
            return results;
        } else if (pointcut instanceof Pointcut.And(Pointcut origin, Pointcut other)) {
            final JSONObject result = new JSONObject();
            for (final JSONObject originJson : this.toJSON(origin)) {
                final JSONArray keys = originJson.names();
                IntStream.range(0, keys.length())
                        .forEach(i -> this.mergeAndCondition(result, originJson, keys.getString(i)));
            }
            for (final JSONObject otherJson : this.toJSON(other)) {
                final JSONArray keys = otherJson.names();
                IntStream.range(0, keys.length())
                        .forEach(i -> this.mergeAndCondition(result, otherJson, keys.getString(i)));
            }
            return List.of(result);
        } else if (pointcut instanceof Exportable target) {
            results.add(this.toJSON(target));
        }
        return results;
    }

    protected JSONObject toJSON(final Exportable target) {
        final JSONObject result = new JSONObject();
        if (target.stringParams().isEmpty() && target.locatorParams().isEmpty()) {
            if (target.value().isBlank()) {
                result.put(target.key(), new JSONObject());
            } else {
                result.put(target.key(), target.value());
            }
        } else {
            final JSONObject values = new JSONObject();
            target.stringParams().forEach(values::put);
            target.locatorParams().forEach((key, value) -> values.put(key, this.toJSON(value)));
            result.put(target.key(), values);
        }
        return result;
    }

    protected void mergeAndCondition(final JSONObject result, final JSONObject origin, final String key) {
        if (result.has(key)) {
            final Object obj = result.get(key);
            if (key.startsWith("verify")) {
                if (obj instanceof JSONArray arrayObj) {
                    arrayObj.put(origin.get(key));
                } else {
                    result.remove(key);
                    final JSONArray newArray = new JSONArray();
                    newArray.put(obj);
                    newArray.put(origin.get(key));
                    result.put(key, newArray);
                }
            } else {
                if (obj instanceof JSONObject valueObj) {
                    final JSONObject addObj = origin.getJSONObject(key);
                    addObj.names().toList()
                            .stream()
                            .map(Object::toString)
                            .filter(it -> valueObj.has(it) && !valueObj.get(it).equals(addObj.get(it)))
                            .forEach(it -> this.mergeListableValue(valueObj, addObj, it, valueObj.get(it)));
                } else {
                    this.mergeListableValue(result, origin, key, obj);
                }
            }
        } else {
            result.put(key, origin.get(key));
        }
    }

    protected void mergeListableValue(final JSONObject valueObj, final JSONObject addObj, final String objectKey, final Object o) {
        if (o instanceof String value) {
            valueObj.remove(objectKey);
            final JSONArray values = new JSONArray();
            values.put(value);
            this.mergeToArray(addObj, objectKey, values);
            valueObj.put(objectKey, values);
        } else if (o instanceof JSONArray values) {
            this.mergeToArray(addObj, objectKey, values);
        }
    }

    protected void mergeToArray(final JSONObject origin, final String key, final JSONArray values) {
        if (origin.get(key) instanceof String addValue) {
            values.put(addValue);
        } else if (origin.get(key) instanceof JSONArray addValues) {
            IntStream.range(0, addValues.length())
                    .mapToObj(addValues::get)
                    .forEach(values::put);
        }
    }
}
