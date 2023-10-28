package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.TestCase;
import com.sebuilder.interpreter.TestCaseBuilder;
import org.json.JSONObject;

import java.io.File;

public class OverrideSettingLoader {

    private final AspectLoader aspectLoader;

    private final PointcutLoader pointcutLoader;

    private final DataSourceConfigLoader dataSourceConfigLoader;

    public OverrideSettingLoader(final AspectLoader aspectLoader, final DataSourceConfigLoader dataSourceConfigLoader) {
        this.aspectLoader = aspectLoader;
        this.pointcutLoader = aspectLoader.pointcutLoader();
        this.dataSourceConfigLoader = dataSourceConfigLoader;
    }

    public TestCase load(final JSONObject script, final File baseDir, final TestCase resultTestCase) {
        return resultTestCase.map(builder -> builder.setOverrideSetting(
                        new TestCaseBuilder()
                                .setOverrideTestDataSet(this.dataSourceConfigLoader.getDataSource(script)
                                        , this.dataSourceConfigLoader.getDataSourceConfig(script))
                                .setSkip(this.getSkip(script))
                                .isNestedChain(this.isNestedChain(script))
                                .isBreakNestedChain(this.isBreakNestedChain(script))
                                .isPreventContextAspect(this.isPreventContextAspect(script))
                                .mapWhen(it -> script.has("include")
                                        , it -> it.setIncludeTestRun(this.pointcutLoader.load(script, "include", baseDir)
                                                .orElse(Pointcut.ANY)))
                                .mapWhen(it -> script.has("exclude")
                                        , it -> it.setExcludeTestRun(this.pointcutLoader.load(script, "exclude", baseDir)
                                                .orElse(Pointcut.NONE)))
                                .mapWhen(it -> script.has("aspect")
                                        , it -> it.setAspect(this.aspectLoader.load(script, baseDir)
                                                .takeOverChain(false)))
                                .build()
                )
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
