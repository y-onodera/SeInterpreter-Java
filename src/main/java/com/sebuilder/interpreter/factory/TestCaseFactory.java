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
package com.sebuilder.interpreter.factory;

import com.google.common.base.Strings;
import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.util.HashMap;

/**
 * Factory to create TestCase objects from a string, a reader or JSONObject.
 *
 * @author jkowalczyk
 */
public class TestCaseFactory {

    private StepTypeFactory stepTypeFactory = new StepTypeFactory();

    private DataSourceFactory dataSourceFactory = new DataSourceFactory();

    private AspectFactory aspectFactory = new AspectFactory(this.stepTypeFactory);

    public StepTypeFactory getStepTypeFactory() {
        return this.stepTypeFactory;
    }

    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public AspectFactory getAspectFactory() {
        return aspectFactory;
    }

    /**
     * @param jsonString A JSON string describing a script or suite.
     * @return A script, ready to run.
     * @throws IOException   If anything goes wrong with interpreting the JSON, or
     *                       with the Reader.
     * @throws JSONException If the JSON can't be parsed.
     */
    public TestCase parse(String jsonString) throws IOException, JSONException {
        return this.parse(new JSONObject(new JSONTokener(jsonString)), null).iterator().next();
    }

    /**
     * @param json A JSON string describing a script or suite.
     * @return A list of script, ready to run.
     * @throws IOException   If anything goes wrong with interpreting the JSON, or
     *                       with the Reader.
     * @throws JSONException If the JSON can't be parsed.
     */
    public Suite parse(String json, File file) throws IOException, JSONException {
        return this.parse(new JSONObject(new JSONTokener(json)), file);
    }

    /**
     * @param f A File pointing to a JSON file describing a script or suite.
     * @return A list of script, ready to run.
     * @throws IOException   If anything goes wrong with interpreting the JSON, or
     *                       with the Reader.
     * @throws JSONException If the JSON can't be parsed.
     */
    public Suite parse(File f) throws IOException, JSONException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "UTF-8"))) {
            return this.parse(new JSONObject(new JSONTokener(r)), f);
        }
    }

    /**
     * @param o          A JSONObject describing a script or a suite.
     * @param sourceFile Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    private Suite parse(JSONObject o, File sourceFile) throws IOException {
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
    private Suite parseSuite(JSONObject o, File suiteFile) throws IOException {
        try {
            DataSource dataSource = this.dataSourceFactory.getDataSource(o);
            HashMap<String, String> config = this.dataSourceFactory.getDataSourceConfig(o);
            SuiteBuilder builder = new SuiteBuilder(suiteFile)
                    .setShareState(o.optBoolean("shareState", true))
                    .setDataSource(dataSource, config);
            this.loadScripts(o, builder);
            return builder.setAspect(this.aspectFactory.getAspect(o))
                    .createSuite();
        } catch (JSONException e) {
            throw new IOException("Could not parse suite.", e);
        }
    }

    /**
     * @param o A JSONObject describing a script or a suite.
     * @param f Optionally. the file the JSON was loaded from.
     * @return A script, ready to run.
     * @throws IOException If anything goes wrong with interpreting the JSON.
     */
    private Suite parseScript(JSONObject o, File f) throws IOException {
        try {
            return this.create(o, f).toSuite();
        } catch (JSONException e) {
            throw new IOException("Could not parse script.", e);
        }
    }

    /**
     * @param saveTo file script save to
     * @return A new instance of script
     */
    private TestCase create(JSONObject o, File saveTo) throws JSONException {
        DataSource dataSource = this.dataSourceFactory.getDataSource(o);
        HashMap<String, String> config = this.dataSourceFactory.getDataSourceConfig(o);
        TestCase testCase = new TestCaseBuilder()
                .addSteps(this.getStepTypeFactory().parseStep(o))
                .associateWith(saveTo)
                .setDataSource(dataSource, config)
                .build();
        return testCase;
    }

    private void loadScripts(JSONObject o, SuiteBuilder builder) throws IOException, JSONException {
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
                builder.addTests(this.loadScript(script, builder.getSuiteFile()));
            }
        }
    }

    /**
     * @param script A JSONObject describing a script or a suite where load from.
     * @return loaded from file
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     * @throws IOException   If script file not found.
     */
    private Suite loadScript(JSONObject script, File suiteFile) throws JSONException, IOException {
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

    private void loadScriptChain(JSONArray scriptArrays, SuiteBuilder builder) throws JSONException, IOException {
        TestCase lastLoad = null;
        for (int j = 0; j < scriptArrays.length(); j++) {
            for (TestCase loaded : this.loadScript(scriptArrays.getJSONObject(j), builder.getSuiteFile())) {
                if (lastLoad != null) {
                    builder.testChain(lastLoad, loaded);
                }
                builder.addTest(loaded);
                lastLoad = loaded;
            }
        }
    }

    private Suite loadScriptIfExists(File wherePath, JSONObject script) throws IOException, JSONException {
        if (wherePath.exists()) {
            Suite result = this.parse(wherePath);
            return result.replace(this.overrideSetting(script, result.get(0)));
        }
        throw new IOException("TestCase file " + wherePath.toString() + " not found.");
    }

    private TestCase overrideSetting(JSONObject script, TestCase resultTestCase) throws JSONException {
        DataSource dataSource = this.dataSourceFactory.getDataSource(script);
        if (dataSource != null) {
            HashMap<String, String> config = this.dataSourceFactory.getDataSourceConfig(script);
            resultTestCase = resultTestCase.builder()
                    .overrideDataSource(dataSource, config)
                    .build();
        }
        resultTestCase = resultTestCase.skip(this.getSkip(script))
                .nestedChain(this.isNestedChain(script))
                .breakNestedChain(this.isBreakNestedChain(script))
        ;
        return resultTestCase;
    }

    private boolean isNestedChain(JSONObject script) throws JSONException {
        boolean result = false;
        if (script.has("nestedChain")) {
            result = script.getBoolean("nestedChain");
        }
        return result;
    }

    private boolean isBreakNestedChain(JSONObject script) throws JSONException {
        boolean result = false;
        if (script.has("breakNestedChain")) {
            result = script.getBoolean("breakNestedChain");
        }
        return result;
    }

    private String getSkip(JSONObject o) throws JSONException {
        String result = "false";
        if (o.has("skip")) {
            result = o.getString("skip");
        }
        return result;
    }
}