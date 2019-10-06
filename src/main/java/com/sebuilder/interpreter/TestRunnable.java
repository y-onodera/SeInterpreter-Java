package com.sebuilder.interpreter;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface TestRunnable<T extends TestRunnable> extends Iterable<TestCase> {

    default void run(TestRunner runner, TestRunListener testRunListener) {
        for (TestRunBuilder testRunBuilder : this.createTestRunBuilder()) {
            for (TestData data : testRunBuilder.loadData()) {
                boolean stop = runner.execute(testRunBuilder.copy(), data, testRunListener);
                if (stop) {
                    break;
                }
            }
        }
    }

    default List<TestData> loadData() {
        return this.loadData(this.getShareInput());
    }

    default List<TestData> loadData(TestData vars) {
        if (this.getOverrideTestDataSet().getDataSource() != null) {
            return this.getOverrideTestDataSet().loadData(vars);
        }
        return this.getTestDataSet().loadData(vars);
    }

    Suite toSuite();

    TestCase head();

    TestRunBuilder[] createTestRunBuilder();

    TestRunBuilder[] createTestRunBuilder(Scenario aScenario);

    String name();

    String path();

    String fileName();

    ScriptFile getScriptFile();

    default File relativePath() {
        return this.getScriptFile().relativePath();
    }

    TestData getShareInput();

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

    T shareInput(TestData testData);

    T overrideDataSource(DataSource dataSource, Map<String, String> config);

    T nestedChain(boolean nestedChain);

    T breakNestedChain(boolean breakNestedChain);

    Builder<T> builder();

    interface Builder<T extends TestRunnable> {

        T build();

        Builder<T> setScriptFile(ScriptFile of);

        Builder<T> setName(String newName);

        Builder<T> associateWith(File target);

        Builder<T> setShareInput(TestData testData);

        Builder<T> addDataSourceConfig(String key, String value);

        Builder<T> setDataSource(DataSource dataSource, Map<String, String> config);

        Builder<T> isShareState(boolean isShareState);

        Builder<T> setSkip(String skip);

        Builder<T> setOverrideTestDataSet(DataSource dataSource, Map<String, String> dataSourceConfig);

        Builder<T> setLazyLoad(Function<TestData, TestRunnable> lazyLoad);

        Builder<T> isNestedChain(boolean nestedChain);

        Builder<T> isBreakNestedChain(boolean breakNestedChain);

        ScriptFile getScriptFile();

        TestData getShareInput();

        TestDataSet getTestDataSet();

        boolean isShareState();

        String getSkip();

        TestDataSet getOverrideTestDataSet();

        Function<TestData, TestRunnable> getLazyLoad();

        boolean isNestedChain();

        boolean isBreakNestedChain();

    }
}
