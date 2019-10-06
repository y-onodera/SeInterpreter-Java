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
import com.sebuilder.interpreter.pointcut.LocatorFilter;
import com.sebuilder.interpreter.pointcut.NegatedFilter;
import com.sebuilder.interpreter.pointcut.StepTypeFilter;
import com.sebuilder.interpreter.pointcut.StringParamFilter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Factory to create TestCase objects from a string, a reader or JSONObject.
 *
 * @author jkowalczyk
 */
public class Sebuilder implements ScriptParser {

    private SebuilderToStringConverter sebuilderToStringConverter = new SebuilderToStringConverter();

    @Override
    public String toString(Suite target) {
        return this.sebuilderToStringConverter.toString(target);
    }

    @Override
    public String toString(TestCase target) {
        return this.sebuilderToStringConverter.toString(target);
    }

    /**
     * @param jsonString A JSON string describing a script or suite.
     * @return A script, ready to run.
     */
    @Override
    public TestCase load(String jsonString) {
        try {
            return this.load(new JSONObject(new JSONTokener(jsonString)), null).iterator().next();
        } catch (IOException | JSONException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * @param f A File pointing to a JSON file describing a script or suite.
     * @return A list of script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON, or
     *                     with the Reader.
     */
    @Override
    public Suite load(File f) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            try {
                return this.load(new JSONObject(new JSONTokener(r)), f);
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * @param json A JSON string describing a script or suite.
     * @return A list of script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON, or
     *                     with the Reader.
     */
    @Override
    public Suite load(String json, File file) throws IOException {
        try {
            return this.load(new JSONObject(new JSONTokener(json)), file);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    @Override
    public Aspect loadAspect(File f) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            try {
                return this.getAspect(new JSONObject(new JSONTokener(r)));
            } catch (JSONException e) {
                throw new IOException(e);
            }
        }
    }

    /**
     * @param o          A JSONObject describing a script or a suite.
     * @param sourceFile Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    protected Suite load(JSONObject o, File sourceFile) throws IOException {
        if (o.optString("type", "script").equals("suite")) {
            return this.parseSuite(o, sourceFile);
        }
        return this.parseScript(o, sourceFile);
    }

    /**
     * @param o         A JSONObject describing a script or a suite.
     * @param suiteFile Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    protected Suite parseSuite(JSONObject o, File suiteFile) throws IOException {
        try {
            DataSource dataSource = this.getDataSource(o);
            HashMap<String, String> config = this.getDataSourceConfig(o);
            SuiteBuilder builder = new SuiteBuilder(suiteFile)
                    .isShareState(o.optBoolean("shareState", true))
                    .setDataSource(dataSource, config);
            this.loadScripts(o, builder);
            return builder.setAspect(this.getAspect(o))
                    .build();
        } catch (JSONException e) {
            throw new IOException("Could not load suite.", e);
        }
    }

    /**
     * @param o A JSONObject describing a script or a suite.
     * @param f Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    protected Suite parseScript(JSONObject o, File f) throws IOException {
        try {
            return this.create(o, f).toSuite();
        } catch (JSONException e) {
            throw new IOException("Could not load script.", e);
        }
    }

    /**
     * @param saveTo file script save to
     * @return A new instance of script
     */
    protected TestCase create(JSONObject o, File saveTo) throws JSONException {
        DataSource dataSource = this.getDataSource(o);
        HashMap<String, String> config = this.getDataSourceConfig(o);
        TestCase testCase = new TestCaseBuilder()
                .addSteps(this.parseStep(o))
                .associateWith(saveTo)
                .setDataSource(dataSource, config)
                .build();
        return testCase;
    }

    protected void loadScripts(JSONObject o, SuiteBuilder builder) throws IOException, JSONException {
        JSONArray scriptLocations = o.getJSONArray("scripts");
        for (int i = 0; i < scriptLocations.length(); i++) {
            JSONObject script = scriptLocations.getJSONObject(i);
            if (script.has("paths")) {
                JSONArray scriptArrays = script.getJSONArray("paths");
                this.loadScriptChain(scriptArrays, builder);
            } else if (script.has("chain")) {
                JSONArray scriptArrays = script.getJSONArray("chain");
                this.loadScriptChain(scriptArrays, builder);
            } else {
                builder.addTest(this.loadScript(script, new File(builder.getScriptFile().path())));
            }
        }
    }

    /**
     * @param script A JSONObject describing a script or a suite where load from.
     * @return loaded from file
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     * @throws IOException   If script file not found.
     */
    protected Suite loadScript(JSONObject script, File suiteFile) throws JSONException, IOException {
        if (script.has("lazyLoad")) {
            String beforeReplace = script.getString("lazyLoad");
            final TestCase resultTestCase = TestCaseBuilder.lazyLoad(beforeReplace, (TestData data) -> {
                String fileName = data.bind(beforeReplace);
                JSONObject source = new JSONObject();
                try {
                    source.put("path", fileName);
                    return loadScript(source, suiteFile).get(0);
                } catch (JSONException | IOException e) {
                    throw new AssertionError(e);
                }
            });
            return this.overrideSetting(script, resultTestCase).toSuite();
        }
        String path = script.getString("path");
        if (script.has("where") && Strings.isNullOrEmpty(script.getString("where"))) {
            File wherePath = new File(script.getString("where"), path);
            return this.loadScriptIfExists(wherePath, script);
        }
        File f = new File(path);
        if (!f.exists()) {
            f = new File(suiteFile.getAbsoluteFile().getParentFile(), path);
        }
        return this.loadScriptIfExists(f, script);
    }

    protected void loadScriptChain(JSONArray scriptArrays, SuiteBuilder builder) throws JSONException, IOException {
        TestCase lastLoad = null;
        for (int j = 0; j < scriptArrays.length(); j++) {
            for (TestCase loaded : this.loadScript(scriptArrays.getJSONObject(j), new File(builder.getScriptFile().path()))) {
                builder.addTest(loaded);
                TestCase addTest = builder.getScenario().get(builder.getScenario().testCaseSize() - 1);
                if (lastLoad != null) {
                    builder.chain(lastLoad, addTest);
                }
                lastLoad = addTest;
            }
        }
    }

    protected Suite loadScriptIfExists(File wherePath, JSONObject script) throws IOException, JSONException {
        if (wherePath.exists()) {
            Suite result = this.load(wherePath);
            return result.replace(this.overrideSetting(script, result.get(0)));
        }
        throw new IOException("TestCase file " + wherePath.toString() + " not found.");
    }

    protected TestCase overrideSetting(JSONObject script, TestCase resultTestCase) throws JSONException {
        DataSource dataSource = this.getDataSource(script);
        if (dataSource != null) {
            HashMap<String, String> config = this.getDataSourceConfig(script);
            resultTestCase = resultTestCase.overrideDataSource(dataSource, config);
        }
        resultTestCase = resultTestCase.skip(this.getSkip(script))
                .nestedChain(this.isNestedChain(script))
                .breakNestedChain(this.isBreakNestedChain(script))
        ;
        return resultTestCase;
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

    protected String getSkip(JSONObject o) throws JSONException {
        String result = "false";
        if (o.has("skip")) {
            result = o.getString("skip");
        }
        return result;
    }

    protected DataSource getDataSource(JSONObject o) throws JSONException {
        if (!o.has("data")) {
            return null;
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
            for (Iterator<String> it = cfg.keys(); it.hasNext(); ) {
                String key = it.next();
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

    protected Predicate<Step> getPointcut(JSONArray pointcuts) throws JSONException {
        Predicate<Step> result = Aspect.NONE;
        for (int i = 0; i < pointcuts.length(); i++) {
            result = result.or(this.parseFilter(pointcuts.getJSONObject(i)));
        }
        return result;
    }


    protected Predicate<Step> parseFilter(JSONObject pointcutJSON) throws JSONException {
        Predicate<Step> pointcut = Aspect.APPLY;
        JSONArray keysA = pointcutJSON.names();
        Map<String, String> stringParam = Maps.newHashMap();
        Map<String, Locator> locatorParam = Maps.newHashMap();
        for (int j = 0; j < keysA.length(); j++) {
            String key = keysA.getString(j);
            if (key.equals("type")) {
                pointcut = pointcut.and(new StepTypeFilter(pointcutJSON.getString(key)));
            } else if (key.equals("negated")) {
                pointcut = pointcut.and(new NegatedFilter(pointcutJSON.getBoolean(key)));
            } else if (key.startsWith("locator")) {
                JSONObject locatorJSON = pointcutJSON.getJSONObject(key);
                locatorParam.put(key, new Locator(locatorJSON.getString("type"), locatorJSON.getString("value")));
            } else {
                stringParam.put(key, pointcutJSON.getString(key));
            }
        }
        if (locatorParam.size() > 0) {
            pointcut = pointcut.and(new LocatorFilter(locatorParam));
        }
        if (stringParam.size() > 0) {
            pointcut = pointcut.and(new StringParamFilter(stringParam));
        }
        return pointcut;
    }
}