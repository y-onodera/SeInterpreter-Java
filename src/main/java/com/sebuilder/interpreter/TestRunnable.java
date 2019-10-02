package com.sebuilder.interpreter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface TestRunnable<T extends TestRunnable> {

    void accept(TestRunner runner, TestRunListener testRunListener);

    default List<TestData> loadData() {
        return this.loadData(new TestData());
    }

    default List<TestData> loadData(TestData vars) {
        if (this.getOverrideTestDataSet().getDataSource() != null) {
            return this.getOverrideTestDataSet().loadData(vars);
        }
        return this.getTestDataSet().loadData(vars);
    }

    Iterable<TestRunBuilder> createTestRunBuilder();

    Iterable<TestRunBuilder> createTestRunBuilder(Scenario scenario);

    String name();

    String path();

    String fileName();

    ScriptFile getScriptFile();

    default File relativePath() {
        return this.getScriptFile().relativePath();
    }

    TestDataSet getTestDataSet();

    boolean isShareState();

    String getSkip();

    boolean skipRunning(TestData param);

    TestDataSet getOverrideTestDataSet();

    Function<TestData, TestRunnable> getLazyLoad();

    default boolean isLazyLoad() {
        return this.getLazyLoad() != null;
    }

    TestRunnable lazyLoad(TestData aSource);

    boolean isNestedChain();

    boolean isBreakNestedChain();

    T rename(String s);

    T shareState(boolean isShareState);

    T skip(String skip);

    T overrideDataSource(DataSource dataSource, Map<String, String> config);

    T nestedChain(boolean nestedChain);

    T breakNestedChain(boolean breakNestedChain);

    Builder<T> builder();

    TestCase testCase();

    interface Builder<T extends TestRunnable> {

        T build();

        Builder<T> setScriptFile(ScriptFile of);

        Builder<T> setName(String newName);

        Builder<T> associateWith(File target);

        Builder<T> addDataSourceConfig(String key, String value);

        Builder<T> setDataSource(DataSource dataSource, Map<String, String> config);

        Builder<T> isShareState(boolean isShareState);

        Builder<T> setSkip(String skip);

        Builder<T> setOverrideTestDataSet(DataSource dataSource, Map<String, String> dataSourceConfig);

        Builder<T> setLazyLoad(Function<TestData, TestRunnable> lazyLoad);

        Builder<T> isNestedChain(boolean nestedChain);

        Builder<T> isBreakNestedChain(boolean breakNestedChain);

        ScriptFile getScriptFile();

        TestDataSet getTestDataSet();

        boolean isShareState();

        String getSkip();

        TestDataSet getOverrideTestDataSet();

        Function<TestData, TestRunnable> getLazyLoad();

        boolean isNestedChain();

        boolean isBreakNestedChain();

    }
}
