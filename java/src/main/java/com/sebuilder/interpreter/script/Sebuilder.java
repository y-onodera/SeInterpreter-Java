/*
 * Copyright 2012 Sauce Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sebuilder.interpreter.script;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.pointcut.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Factory to create TestCase objects from a string, a reader or JSONObject.
 *
 * @author jkowalczyk
 */
public class Sebuilder extends AbstractJsonScriptParser {

    /**
     * @param jsonString A JSON string describing a script or suite.
     * @return A script, ready to finish.
     */
    @Override
    public TestCase load(final String jsonString) {
        try {
            return this.parseScript(new JSONObject(new JSONTokener(jsonString)), null);
        } catch (final Throwable e) {
            throw new AssertionError("error parse:" + jsonString, e);
        }
    }

    @Override
    public Aspect loadAspect(final File f) {
        try (final BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            return this.getAspect(new JSONObject(new JSONTokener(r)));
        } catch (final Throwable e) {
            throw new AssertionError("error load:" + f.getAbsolutePath(), e);
        }
    }

    @Override
    protected TestCase load(final JSONObject o, final File sourceFile) {
        if (o.optString("type", "script").equals("suite")) {
            return this.parseSuite(o, sourceFile);
        }
        return this.parseScript(o, sourceFile);
    }

    /**
     * @param o         A JSONObject describing a script or a suite.
     * @param suiteFile Optionally. the file the JSON was loaded from.
     * @return A script, ready to finish.
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected TestCase parseSuite(final JSONObject o, final File suiteFile) {
        final TestCaseBuilder builder = TestCaseBuilder.suite(suiteFile)
                .setDataSource(this.getDataSource(o), this.getDataSourceConfig(o));
        this.loadScripts(o, builder);
        return builder.setAspect(this.getAspect(o)).build();
    }

    /**
     * @param o A JSONObject describing a script or a suite.
     * @param f Optionally. the file the JSON was loaded from.
     * @return A script, ready to finish.
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected TestCase parseScript(final JSONObject o, final File f) {
        return this.create(o, f);
    }

    /**
     * @param saveTo file script save to
     * @return A new instance of script
     */
    protected TestCase create(final JSONObject o, final File saveTo) {
        return new TestCaseBuilder()
                .addSteps(this.parseStep(o))
                .associateWith(saveTo)
                .setDataSource(this.getDataSource(o), this.getDataSourceConfig(o))
                .build();
    }

    protected void loadScripts(final JSONObject o, final TestCaseBuilder builder) {
        final JSONArray scriptLocations = o.getJSONArray("scripts");
        IntStream.range(0, scriptLocations.length()).forEach(i -> {
            final JSONObject script = scriptLocations.getJSONObject(i);
            if (script.has("paths")) {
                final JSONArray scriptArrays = script.getJSONArray("paths");
                this.loadScriptChain(scriptArrays, builder);
            } else if (script.has("chain")) {
                final JSONArray scriptArrays = script.getJSONArray("chain");
                this.loadScriptChain(scriptArrays, builder);
            } else {
                builder.addChain(this.loadScript(script, new File(builder.getScriptFile().path())));
            }
        });
    }

    /**
     * @param script A JSONObject describing a script or a suite where load from.
     * @return loaded from file
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected TestCase loadScript(final JSONObject script, final File suiteFile) {
        if (script.has("lazyLoad")) {
            final String beforeReplace = script.getString("lazyLoad");
            return this.overrideSetting(script, TestCaseBuilder.lazyLoad(beforeReplace, (runtimeBefore) -> {
                final String fileName = runtimeBefore.shareInput().evaluateString(beforeReplace);
                final JSONObject source = new JSONObject();
                final TestCase lazyLoad = this.loadScript(source.put("path", fileName), suiteFile);
                return runtimeBefore.map(it -> it
                        .associateWith(lazyLoad.scriptFile().toFile())
                        .setName(lazyLoad.name())
                        .addSteps(lazyLoad.steps())
                        .setTestDataSet(lazyLoad.dataSourceLoader())
                        .addAspect(lazyLoad.aspect())
                );
            }));
        }
        final String path = Context.bindEnvironmentProperties(script.getString("path"));
        if (script.has("where") && Strings.isNullOrEmpty(script.getString("where"))) {
            final File wherePath = new File(Context.bindEnvironmentProperties(script.getString("where")), path);
            return this.loadScriptIfExists(wherePath, script);
        }
        File f = new File(path);
        if (!f.exists()) {
            f = new File(suiteFile.getAbsoluteFile().getParentFile(), path);
        }
        return this.loadScriptIfExists(f.getAbsoluteFile(), script);
    }

    protected void loadScriptChain(final JSONArray scriptArrays, final TestCaseBuilder builder) {
        final ChainLoader chainLoader = new ChainLoader(this, builder.getScriptFile(), scriptArrays);
        builder.addChain(chainLoader.load());
    }

    protected TestCase loadScriptIfExists(final File wherePath, final JSONObject script) {
        return this.overrideSetting(script, this.load(wherePath));
    }

    protected DataSource getDataSource(final JSONObject o) {
        if (!o.has("data")) {
            return DataSource.NONE;
        }
        final JSONObject data = o.getJSONObject("data");
        return Context.getDataSourceFactory().getDataSource(data.getString("source"));
    }

    protected HashMap<String, String> getDataSourceConfig(final JSONObject o) {
        if (!o.has("data")) {
            return new HashMap<>();
        }
        final JSONObject data = o.getJSONObject("data");
        final String sourceName = data.getString("source");
        final HashMap<String, String> config = new HashMap<>();
        if (data.has("configs") && data.getJSONObject("configs").has(sourceName)) {
            final JSONObject cfg = data.getJSONObject("configs").getJSONObject(sourceName);
            cfg.keySet().forEach(key -> config.put(key, cfg.getString(key)));
        }
        return config;
    }

    /**
     * @param o json object step load from
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected ArrayList<Step> parseStep(final JSONObject o) {
        final JSONArray stepsA = o.getJSONArray("steps");
        return IntStream.range(0, stepsA.length())
                .mapToObj(i -> this.parseStep(stepsA, i))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected Step parseStep(final JSONArray steps, final int i) {
        return this.createStep(steps.getJSONObject(i));
    }

    /**
     * @param stepO json object step load from
     * @return A new instance of step
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected Step createStep(final JSONObject stepO) {
        final StepType type = Context.getStepTypeFactory().getStepTypeOfName(stepO.getString("type"));
        final boolean isNegated = stepO.optBoolean("negated", false);
        final String name = stepO.optString("step_name", null);
        final StepBuilder step = new StepBuilder(name, type, isNegated);
        final JSONArray keysA = stepO.names();
        IntStream.range(0, keysA.length())
                .mapToObj(keysA::getString)
                .filter(key -> !key.equals("type") && !key.equals("negated"))
                .forEach(key -> {
                    if (stepO.optJSONObject(key) != null && key.startsWith("locator")) {
                        step.put(key, new Locator(stepO.getJSONObject(key).getString("type"), stepO.getJSONObject(key).getString("value")));
                    } else {
                        step.put(key, stepO.getString(key));
                    }
                });
        return step.build();
    }

    protected TestCase overrideSetting(final JSONObject script, final TestCase resultTestCase) {
        final DataSource dataSource = this.getDataSource(script);
        final HashMap<String, String> config = this.getDataSourceConfig(script);
        final String skip = Sebuilder.this.getSkip(script);
        final boolean nestedChain = this.isNestedChain(script);
        final boolean breakNestedChain = this.isBreakNestedChain(script);
        final boolean preventContextAspect = this.isPreventContextAspect(script);
        return resultTestCase.map(it -> it.setSkip(skip)
                .mapWhen(target -> dataSource != null
                        , matches -> matches.setOverrideTestDataSet(dataSource, config)
                )
                .isNestedChain(nestedChain)
                .isBreakNestedChain(breakNestedChain)
                .isPreventContextAspect(preventContextAspect)
        );
    }

    protected String getSkip(final JSONObject o) {
        String result = "false";
        if (o.has("skip")) {
            result = o.getString("skip");
        }
        return result;
    }

    protected boolean isNestedChain(final JSONObject script) {
        boolean result = false;
        if (script.has("nestedChain")) {
            result = script.getBoolean("nestedChain");
        }
        return result;
    }

    protected boolean isBreakNestedChain(final JSONObject script) {
        boolean result = false;
        if (script.has("breakNestedChain")) {
            result = script.getBoolean("breakNestedChain");
        }
        return result;
    }

    protected boolean isPreventContextAspect(final JSONObject script) {
        boolean result = false;
        if (script.has("preventContextAspect")) {
            result = script.getBoolean("preventContextAspect");
        }
        return result;
    }

    protected Aspect getAspect(final JSONObject o) {
        Aspect result = new Aspect();
        if (o.has("aspect")) {
            final Aspect.Builder builder = result.builder();
            final JSONArray aspects = o.getJSONArray("aspect");
            IntStream.range(0, aspects.length()).forEach(i ->
                    builder.add(() -> {
                        final ExtraStepExecuteInterceptor.Builder interceptorBuilder = new ExtraStepExecuteInterceptor.Builder();
                        final JSONObject aspect = aspects.getJSONObject(i);
                        if (aspect.has("pointcut")) {
                            interceptorBuilder.setPointcut(this.getPointcut(aspect.getJSONArray("pointcut")));
                        }
                        if (aspect.has("before")) {
                            interceptorBuilder.addBefore(this.load(aspect.getJSONObject("before"), null));
                        }
                        if (aspect.has("after")) {
                            interceptorBuilder.addAfter(this.load(aspect.getJSONObject("after"), null));
                        }
                        if (aspect.has("failure")) {
                            interceptorBuilder.addFailure(this.load(aspect.getJSONObject("failure"), null));
                        }
                        return interceptorBuilder.get();
                    })
            );
            result = builder.build();
        }
        return result;
    }

    protected Pointcut getPointcut(final JSONArray pointcuts) {
        return IntStream.range(0, pointcuts.length())
                .mapToObj(i -> this.parseFilter(pointcuts.getJSONObject(i)))
                .reduce(Pointcut.NONE, Pointcut::or);
    }


    protected Pointcut parseFilter(final JSONObject pointcutJSON) {
        final JSONArray keysA = pointcutJSON.names();
        return IntStream.range(0, keysA.length())
                .mapToObj(j -> this.parseFilter(pointcutJSON, keysA, j))
                .reduce(Pointcut.ANY, Pointcut::and);
    }

    protected Pointcut parseFilter(final JSONObject pointcutJSON, final JSONArray keysA, final int j) {
        final String key = keysA.getString(j);
        if (key.equals("type")) {
            return this.getStepTypeFilter(pointcutJSON, key);
        } else if (key.equals("negated")) {
            return new NegatedFilter(pointcutJSON.getBoolean(key));
        } else if (key.equals("skip")) {
            return new SkipFilter(pointcutJSON.getBoolean(key));
        } else if (key.startsWith("locator")) {
            return this.getLocatorFilter(pointcutJSON, key);
        }
        return this.getStringFilter(pointcutJSON, key);
    }

    protected Pointcut getStringFilter(final JSONObject pointcutJSON, final String key) {
        if (pointcutJSON.get(key) instanceof JSONArray) {
            final JSONArray type = pointcutJSON.getJSONArray(key);
            return IntStream.range(0, type.length())
                    .mapToObj(k -> (Pointcut) new StringParamFilter(key, type.getString(k)))
                    .reduce(Pointcut.NONE, Pointcut::or);
        } else if (pointcutJSON.get(key) instanceof JSONObject) {
            final JSONObject type = pointcutJSON.getJSONObject(key);
            return new StringParamFilter(key, type.getString("value"), type.getString(Pointcut.METHOD_KEY));
        }
        return new StringParamFilter(key, pointcutJSON.getString(key));
    }

    protected Pointcut getLocatorFilter(final JSONObject pointcutJSON, final String key) {
        final JSONObject locatorJSON = pointcutJSON.getJSONObject(key);
        if (locatorJSON.get("value") instanceof JSONArray) {
            final JSONArray values = locatorJSON.getJSONArray("value");
            return IntStream.range(0, values.length())
                    .mapToObj(k -> {
                        final Locator locator = new Locator(locatorJSON.getString("type"), values.getString(k));
                        return (Pointcut) new LocatorFilter(key, locator);
                    })
                    .reduce(Pointcut.NONE, Pointcut::or);
        }
        final Locator locator = new Locator(locatorJSON.getString("type"), locatorJSON.getString("value"));
        if (locatorJSON.has("method")) {
            return new LocatorFilter(key, locator, locatorJSON.getString("method"));
        }
        return new LocatorFilter(key, locator);
    }

    protected Pointcut getStepTypeFilter(final JSONObject pointcutJSON, final String key) {
        if (pointcutJSON.get(key) instanceof JSONArray) {
            final JSONArray type = pointcutJSON.getJSONArray(key);
            return IntStream.range(0, type.length())
                    .mapToObj(k -> (Pointcut) new StepTypeFilter(type.getString(k)))
                    .reduce(Pointcut.NONE, Pointcut::or);
        } else if (pointcutJSON.get(key) instanceof JSONObject) {
            final JSONObject type = pointcutJSON.getJSONObject(key);
            return new StepTypeFilter(type.getString("value"), type.getString("method"));
        }
        return new StepTypeFilter(pointcutJSON.getString(key));
    }

}