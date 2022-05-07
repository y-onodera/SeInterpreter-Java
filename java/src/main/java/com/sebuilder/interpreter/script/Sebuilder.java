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
import com.google.common.collect.Maps;
import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.pointcut.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

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
    public TestCase load(String jsonString) {
        try {
            return this.parseScript(new JSONObject(new JSONTokener(jsonString)), null);
        } catch (JSONException e) {
            throw new AssertionError("error parse:" + jsonString, e);
        }
    }

    @Override
    public Aspect loadAspect(File f) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            try {
                return this.getAspect(new JSONObject(new JSONTokener(r)));
            } catch (JSONException e) {
                throw new IOException("error load:" + f.getAbsolutePath(), e);
            }
        }
    }

    @Override
    protected TestCase load(JSONObject o, File sourceFile, TestRunListener testRunListener) throws JSONException, IOException {
        if (o.optString("type", "script").equals("suite")) {
            return this.parseSuite(o, sourceFile, testRunListener);
        }
        return this.parseScript(o, sourceFile);
    }

    /**
     * @param o               A JSONObject describing a script or a suite.
     * @param suiteFile       Optionally. the file the JSON was loaded from.
     * @param testRunListener listener to report error
     * @return A script, ready to finish.
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected TestCase parseSuite(JSONObject o, File suiteFile, TestRunListener testRunListener) throws JSONException, IOException {
        TestCaseBuilder builder = TestCaseBuilder.suite(suiteFile)
                .setDataSource(this.getDataSource(o), this.getDataSourceConfig(o));
        this.loadScripts(o, builder, testRunListener);
        return builder.setAspect(this.getAspect(o))
                .build();
    }

    /**
     * @param o A JSONObject describing a script or a suite.
     * @param f Optionally. the file the JSON was loaded from.
     * @return A script, ready to finish.
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected TestCase parseScript(JSONObject o, File f) throws JSONException {
        return this.create(o, f);
    }

    /**
     * @param saveTo file script save to
     * @return A new instance of script
     */
    protected TestCase create(JSONObject o, File saveTo) throws JSONException {
        return new TestCaseBuilder()
                .addSteps(this.parseStep(o))
                .associateWith(saveTo)
                .setDataSource(this.getDataSource(o), this.getDataSourceConfig(o))
                .build();
    }

    protected void loadScripts(JSONObject o, TestCaseBuilder builder, TestRunListener testRunListener) throws JSONException, IOException {
        JSONArray scriptLocations = o.getJSONArray("scripts");
        for (int i = 0; i < scriptLocations.length(); i++) {
            JSONObject script = scriptLocations.getJSONObject(i);
            if (script.has("paths")) {
                JSONArray scriptArrays = script.getJSONArray("paths");
                this.loadScriptChain(scriptArrays, builder, testRunListener);
            } else if (script.has("chain")) {
                JSONArray scriptArrays = script.getJSONArray("chain");
                this.loadScriptChain(scriptArrays, builder, testRunListener);
            } else {
                builder.addChain(this.loadScript(script, new File(builder.getScriptFile().path()), testRunListener));
            }
        }
    }

    /**
     * @param script          A JSONObject describing a script or a suite where load from.
     * @param testRunListener listener to report error
     * @return loaded from file
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected TestCase loadScript(JSONObject script, File suiteFile, TestRunListener testRunListener) throws JSONException, IOException {
        if (script.has("lazyLoad")) {
            String beforeReplace = script.getString("lazyLoad");
            return this.overrideSetting(script, TestCaseBuilder.lazyLoad(beforeReplace, (runtimeBefore, listener) -> {
                String fileName = runtimeBefore.getShareInput().bind(beforeReplace);
                JSONObject source = new JSONObject();
                try {
                    TestCase lazyLoad = loadScript(source.put("path", fileName), suiteFile, listener);
                    return runtimeBefore.map(it -> it
                            .associateWith(lazyLoad.getScriptFile().toFile())
                            .setName(lazyLoad.name())
                            .addSteps(lazyLoad.steps())
                            .setTestDataSet(lazyLoad.getDataSourceLoader())
                            .addAspect(lazyLoad.getAspect())
                    );
                } catch (JSONException | IOException e) {
                    throw new AssertionError(e);
                }
            }));
        }
        String path = Context.bindEnvironmentProperties(script.getString("path"));
        if (script.has("where") && Strings.isNullOrEmpty(script.getString("where"))) {
            File wherePath = new File(Context.bindEnvironmentProperties(script.getString("where")), path);
            return this.loadScriptIfExists(wherePath, script, testRunListener);
        }
        File f = new File(path);
        if (!f.exists()) {
            f = new File(suiteFile.getAbsoluteFile().getParentFile(), path);
        }
        return this.loadScriptIfExists(f.getAbsoluteFile(), script, testRunListener);
    }

    protected void loadScriptChain(JSONArray scriptArrays, TestCaseBuilder builder, TestRunListener testRunListener) throws JSONException, IOException {
        ChainLoader chainLoader = new ChainLoader(this, builder.getScriptFile(), scriptArrays, testRunListener);
        builder.addChain(chainLoader.load());
    }

    protected TestCase loadScriptIfExists(File wherePath, JSONObject script, TestRunListener testRunListener) throws JSONException, IOException {
        return this.overrideSetting(script, this.load(wherePath, testRunListener));
    }

    protected DataSource getDataSource(JSONObject o) throws JSONException {
        if (!o.has("data")) {
            return DataSource.NONE;
        }
        JSONObject data = o.getJSONObject("data");
        return Context.getDataSourceFactory().getDataSource(data.getString("source"));
    }

    protected HashMap<String, String> getDataSourceConfig(JSONObject o) throws JSONException {
        if (!o.has("data")) {
            return Maps.newHashMap();
        }
        JSONObject data = o.getJSONObject("data");
        String sourceName = data.getString("source");
        HashMap<String, String> config = new HashMap<>();
        if (data.has("configs") && data.getJSONObject("configs").has(sourceName)) {
            JSONObject cfg = data.getJSONObject("configs").getJSONObject(sourceName);
            for (Iterator<String> itr = cfg.keys(); itr.hasNext(); ) {
                String key = itr.next();
                config.put(key, cfg.getString(key));
            }
        }
        return config;
    }

    /**
     * @param o json object step load from
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected ArrayList<Step> parseStep(JSONObject o) throws JSONException {
        JSONArray stepsA = o.getJSONArray("steps");
        ArrayList<Step> steps = new ArrayList<>();
        for (int i = 0; i < stepsA.length(); i++) {
            this.parseStep(steps, stepsA.getJSONObject(i));
        }
        return steps;
    }

    /**
     * @param stepO json object step load from
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected void parseStep(ArrayList<Step> steps, JSONObject stepO) throws JSONException {
        StepBuilder step = this.createStep(stepO);
        this.configureStep(stepO, step);
        steps.add(step.build());
    }

    /**
     * @param stepO json object step load from
     * @return A new instance of step
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected StepBuilder createStep(JSONObject stepO) throws JSONException {
        StepType type = Context.getStepTypeFactory().getStepTypeOfName(stepO.getString("type"));
        boolean isNegated = stepO.optBoolean("negated", false);
        String name = stepO.optString("step_name", null);
        return new StepBuilder(name, type, isNegated);
    }

    /**
     * @param stepO json object step configuration load from
     * @param step  step configuration to
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected void configureStep(JSONObject stepO, StepBuilder step) throws JSONException {
        JSONArray keysA = stepO.names();
        for (int j = 0; j < keysA.length(); j++) {
            this.configureStep(stepO, step, keysA.getString(j));
        }
    }

    /**
     * @param stepO json object step configuration load from
     * @param step  step configuration to
     * @param key   configuration key
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected void configureStep(JSONObject stepO, StepBuilder step, String key) throws JSONException {
        if (key.equals("type") || key.equals("negated")) {
            return;
        }
        if (stepO.optJSONObject(key) != null && key.startsWith("locator")) {
            step.put(key, new Locator(stepO.getJSONObject(key).getString("type"), stepO.getJSONObject(key).getString("value")));
        } else if (stepO.optString(key) != null && key.startsWith("imageArea")) {
            step.put(key, new ImageArea(stepO.getString(key)));
        } else {
            step.put(key, stepO.getString(key));
        }
    }

    protected Aspect getAspect(JSONObject o) throws JSONException {
        Aspect result = new Aspect();
        if (o.has("aspect")) {
            Aspect.Builder builder = result.builder();
            JSONArray aspects = o.getJSONArray("aspect");
            for (int i = 0; i < aspects.length(); i++) {
                Interceptor.Builder interceptorBuilder = builder.interceptor();
                JSONObject aspect = aspects.getJSONObject(i);
                if (aspect.has("pointcut")) {
                    interceptorBuilder.setPointcut(this.getPointcut(aspect.getJSONArray("pointcut")));
                }
                if (aspect.has("before")) {
                    interceptorBuilder.addBefore(this.parseStep(aspect.getJSONObject("before")));
                }
                if (aspect.has("after")) {
                    interceptorBuilder.addAfter(this.parseStep(aspect.getJSONObject("after")));
                }
                if (aspect.has("failure")) {
                    interceptorBuilder.addFailure(this.parseStep(aspect.getJSONObject("failure")));
                }
                interceptorBuilder.build();
            }
            result = builder.build();
        }
        return result;
    }

    protected Pointcut getPointcut(JSONArray pointcuts) throws JSONException {
        Pointcut result = Pointcut.NONE;
        for (int i = 0; i < pointcuts.length(); i++) {
            result = result.or(this.parseFilter(pointcuts.getJSONObject(i)));
        }
        return result;
    }


    protected Pointcut parseFilter(JSONObject pointcutJSON) throws JSONException {
        Pointcut pointcut = Pointcut.ANY;
        JSONArray keysA = pointcutJSON.names();
        for (int j = 0; j < keysA.length(); j++) {
            String key = keysA.getString(j);
            if (key.equals("type")) {
                if (pointcutJSON.get(key) instanceof JSONArray) {
                    JSONArray type = pointcutJSON.getJSONArray(key);
                    Pointcut typeList = Pointcut.NONE;
                    for (int k = 0; k < type.length(); k++) {
                        typeList = typeList.or(new StepTypeFilter(type.getString(k)));
                    }
                    pointcut = pointcut.and(typeList);
                } else if (pointcutJSON.get(key) instanceof JSONObject) {
                    JSONObject type = pointcutJSON.getJSONObject(key);
                    pointcut = pointcut.and(new StepTypeFilter(type.getString("value"), type.getString("method")));
                } else {
                    pointcut = pointcut.and(new StepTypeFilter(pointcutJSON.getString(key)));
                }
            } else if (key.equals("negated")) {
                pointcut = pointcut.and(new NegatedFilter(pointcutJSON.getBoolean(key)));
            } else if (key.equals("skip")) {
                pointcut = pointcut.and(new SkipFilter(pointcutJSON.getBoolean(key)));
            } else if (key.startsWith("locator")) {
                JSONObject locatorJSON = pointcutJSON.getJSONObject(key);
                if (locatorJSON.get("value") instanceof JSONArray) {
                    JSONArray values = locatorJSON.getJSONArray("value");
                    Pointcut typeList = Pointcut.NONE;
                    for (int k = 0; k < values.length(); k++) {
                        Locator locator = new Locator(locatorJSON.getString("type"), values.getString(k));
                        typeList = typeList.or(new LocatorFilter(key, locator));
                    }
                    pointcut = pointcut.and(typeList);
                } else {
                    Locator locator = new Locator(locatorJSON.getString("type"), locatorJSON.getString("value"));
                    if (locatorJSON.has("method")) {
                        pointcut = pointcut.and(new LocatorFilter(key, locator, locatorJSON.getString("method")));
                    } else {
                        pointcut = pointcut.and(new LocatorFilter(key, locator));
                    }
                }
            } else {
                if (pointcutJSON.get(key) instanceof JSONArray) {
                    JSONArray type = pointcutJSON.getJSONArray(key);
                    Pointcut typeList = Pointcut.NONE;
                    for (int k = 0; k < type.length(); k++) {
                        typeList = typeList.or(new StringParamFilter(key, type.getString(k)));
                    }
                    pointcut = pointcut.and(typeList);
                } else if (pointcutJSON.get(key) instanceof JSONObject) {
                    JSONObject type = pointcutJSON.getJSONObject(key);
                    pointcut = pointcut.and(new StringParamFilter(key, type.getString("value"), type.getString(Pointcut.METHOD_KEY)));
                } else {
                    pointcut = pointcut.and(new StringParamFilter(key, pointcutJSON.getString(key)));
                }
            }
        }
        return pointcut;
    }

    protected TestCase overrideSetting(JSONObject script, TestCase resultTestCase) throws JSONException {
        final DataSource dataSource = this.getDataSource(script);
        final HashMap<String, String> config = this.getDataSourceConfig(script);
        final String skip = Sebuilder.this.getSkip(script);
        final boolean nestedChain = this.isNestedChain(script);
        final boolean breakNestedChain = this.isBreakNestedChain(script);
        final boolean preventContextAspect = this.isPreventContextAspect(script);
        return resultTestCase.map(it -> it.setSkip(skip)
                .changeWhenConditionMatch(target -> dataSource != null
                        , matches -> matches.setOverrideTestDataSet(dataSource, config)
                )
                .isNestedChain(nestedChain)
                .isBreakNestedChain(breakNestedChain)
                .isPreventContextAspect(preventContextAspect)
        );
    }

    protected String getSkip(JSONObject o) throws JSONException {
        String result = "false";
        if (o.has("skip")) {
            result = o.getString("skip");
        }
        return result;
    }

    protected boolean isNestedChain(JSONObject script) throws JSONException {
        boolean result = false;
        if (script.has("nestedChain")) {
            result = script.getBoolean("nestedChain");
        }
        return result;
    }

    protected boolean isBreakNestedChain(JSONObject script) throws JSONException {
        boolean result = false;
        if (script.has("breakNestedChain")) {
            result = script.getBoolean("breakNestedChain");
        }
        return result;
    }

    protected boolean isPreventContextAspect(JSONObject script) throws JSONException {
        boolean result = false;
        if (script.has("preventContextAspect")) {
            result = script.getBoolean("preventContextAspect");
        }
        return result;
    }
}