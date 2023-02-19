package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.TestCase;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;

public class OverrideSettingLoader {

    private final PointcutLoader pointcutLoader;

    private final DataSourceConfigLoader dataSourceConfigLoader;

    public OverrideSettingLoader(final PointcutLoader pointcutLoader, final DataSourceConfigLoader dataSourceConfigLoader) {
        this.pointcutLoader = pointcutLoader;
        this.dataSourceConfigLoader = dataSourceConfigLoader;
    }

    public TestCase load(final JSONObject script, final File baseDir, final TestCase resultTestCase) {
        final DataSource dataSource = this.dataSourceConfigLoader.getDataSource(script);
        final HashMap<String, String> config = this.dataSourceConfigLoader.getDataSourceConfig(script);
        final String skip = this.getSkip(script);
        final boolean nestedChain = this.isNestedChain(script);
        final boolean breakNestedChain = this.isBreakNestedChain(script);
        final boolean preventContextAspect = this.isPreventContextAspect(script);
        final Pointcut includeTestRun;
        if (script.has("include")) {
            includeTestRun = this.pointcutLoader.load(script.getJSONArray("include"), baseDir).orElse(Pointcut.ANY);
        } else {
            includeTestRun = Pointcut.ANY;
        }
        final Pointcut excludeTestRun;
        if (script.has("exclude")) {
            excludeTestRun = this.pointcutLoader.load(script.getJSONArray("exclude"), baseDir).orElse(Pointcut.NONE);
        } else {
            excludeTestRun = Pointcut.NONE;
        }
        return resultTestCase.map(it -> it.setSkip(skip)
                .mapWhen(target -> dataSource != null
                        , matches -> matches.setOverrideTestDataSet(dataSource, config)
                )
                .setIncludeTestRun(includeTestRun)
                .setExcludeTestRun(excludeTestRun)
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

}
