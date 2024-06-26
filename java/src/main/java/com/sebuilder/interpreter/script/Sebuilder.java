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

import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.File;
import java.util.ArrayList;
import java.util.stream.IntStream;

/**
 * Factory to create TestCase objects from a string, a reader or JSONObject.
 *
 * @author jkowalczyk
 */
public class Sebuilder extends AbstractJsonScriptParser {

    final PointcutLoader pointcutLoader;

    final DataSourceConfigLoader dataSourceConfigLoader;

    final StepLoader stepLoader;

    final OverrideSettingLoader overrideSettingLoader;

    final AspectLoader aspectLoader;

    final ImportLoader importLoader;

    public Sebuilder() {
        this.stepLoader = new StepLoader();
        this.dataSourceConfigLoader = new DataSourceConfigLoader();
        this.importLoader = new ImportLoader();
        this.pointcutLoader = new PointcutLoader(this.importLoader);
        this.aspectLoader = new AspectLoader(this, this.pointcutLoader);
        this.overrideSettingLoader = new OverrideSettingLoader(this.aspectLoader, this.dataSourceConfigLoader);
    }

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
        return this.aspectLoader.load(f);
    }

    @Override
    public Aspect loadAspect(final String jsonText, final File f) {
        return this.aspectLoader.load(jsonText, f);
    }

    @Override
    public Pointcut loadPointCut(final File path, final File parentDir) {
        return this.pointcutLoader.load(path, parentDir).orElse(Pointcut.NONE);
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
                .setDataSource(this.dataSourceConfigLoader.getDataSource(o), this.dataSourceConfigLoader.getDataSourceConfig(o));
        this.loadScripts(o, builder);
        return builder.setAspect(this.aspectLoader.load(o, suiteFile.getAbsoluteFile().getParentFile())).build();
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
                .setDataSource(this.dataSourceConfigLoader.getDataSource(o), this.dataSourceConfigLoader.getDataSourceConfig(o))
                .build();
    }

    protected ArrayList<Step> parseStep(final JSONObject o) {
        return this.stepLoader.load(o);
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
        final File suiteWhere = suiteFile.getAbsoluteFile().getParentFile();
        if (script.has("lazyLoad")) {
            final String beforeReplace = script.getString("lazyLoad");
            return this.overrideSettingLoader.load(script, suiteWhere, TestCaseBuilder.lazyLoad(beforeReplace, (runtimeBefore) -> {
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
        return this.importLoader.load(script, (path, where) ->
                this.loadScriptIfExists(suiteWhere, this.toFile(suiteWhere, where, path), script));
    }

    protected void loadScriptChain(final JSONArray scriptArrays, final TestCaseBuilder builder) {
        final ChainLoader chainLoader = new ChainLoader(this, this.overrideSettingLoader, builder.getScriptFile(), scriptArrays);
        builder.addChain(chainLoader.load());
    }

    protected TestCase loadScriptIfExists(final File suiteWhere, final File wherePath, final JSONObject script) {
        return this.overrideSettingLoader.load(script, suiteWhere, this.load(wherePath));
    }

    protected File toFile(final File baseDir, final String where, final String path) {
        if (where.isBlank()) {
            return this.toFile(baseDir, path);
        }
        return this.toFile(new File(Context.bindEnvironmentProperties(where)), path);
    }

    protected File toFile(final File baseDir, final String path) {
        final String target = Context.bindEnvironmentProperties(path);
        File f = new File(baseDir, target);
        if (!f.exists()) {
            f = new File(target);
        }
        return f.getAbsoluteFile();
    }

}