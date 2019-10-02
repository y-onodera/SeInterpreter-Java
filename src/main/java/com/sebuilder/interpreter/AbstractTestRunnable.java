package com.sebuilder.interpreter;

import com.google.common.base.Objects;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractTestRunnable<T extends TestRunnable> implements TestRunnable<T> {

    private final ScriptFile scriptFile;
    private final TestDataSet testDataSet;
    private final boolean shareState;
    private final String skip;
    private final TestDataSet overrideTestDataSet;
    private final Function<TestData, TestRunnable> lazyLoad;
    private final boolean nestedChain;
    private final boolean breakNestedChain;

    protected AbstractTestRunnable(Builder builder) {
        this.scriptFile = builder.getScriptFile();
        this.testDataSet = builder.getTestDataSet();
        this.shareState = builder.isShareState();
        this.skip = builder.getSkip();
        this.overrideTestDataSet = builder.getOverrideTestDataSet();
        this.lazyLoad = builder.getLazyLoad();
        this.nestedChain = builder.isNestedChain();
        this.breakNestedChain = builder.isBreakNestedChain();
    }

    @Override
    public ScriptFile getScriptFile() {
        return scriptFile;
    }

    @Override
    public TestDataSet getTestDataSet() {
        return testDataSet;
    }

    @Override
    public TestDataSet getOverrideTestDataSet() {
        return overrideTestDataSet;
    }

    @Override
    public String getSkip() {
        return skip;
    }

    @Override
    public Function<TestData, TestRunnable> getLazyLoad() {
        return lazyLoad;
    }

    @Override
    public String name() {
        return this.scriptFile.name();
    }

    @Override
    public String path() {
        return this.scriptFile.path();
    }

    @Override
    public String fileName() {
        return this.getScriptFile().relativePath(this);
    }

    @Override
    public boolean isShareState() {
        return this.shareState;
    }

    @Override
    public boolean skipRunning(TestData param) {
        return param.evaluate(this.skip);
    }

    @Override
    public T skip(String skip) {
        return builder()
                .setSkip(skip)
                .build();
    }

    @Override
    public TestRunnable lazyLoad(TestData aSource) {
        if (this.lazyLoad != null) {
            return this.lazyLoad.apply(aSource)
                    .builder()
                    .setOverrideTestDataSet(this.overrideTestDataSet.getDataSource(), this.overrideTestDataSet.getDataSourceConfig())
                    .setSkip(this.skip)
                    .build();
        }
        return this;
    }

    @Override
    public boolean isNestedChain() {
        return this.nestedChain;
    }

    @Override
    public boolean isBreakNestedChain() {
        return this.breakNestedChain;
    }

    @Override
    public T rename(String newName) {
        return this.builder()
                .setName(newName)
                .build();
    }

    @Override
    public T shareState(boolean isShareState) {
        return this.builder()
                .isShareState(isShareState)
                .build();
    }

    @Override
    public T overrideDataSource(DataSource dataSource, Map<String, String> config) {
        return this.builder()
                .setOverrideTestDataSet(dataSource, config)
                .build();
    }

    @Override
    public T nestedChain(boolean nestedChain) {
        return this.builder()
                .isNestedChain(nestedChain)
                .build();
    }

    @Override
    public T breakNestedChain(boolean breakNestedChain) {
        return this.builder()
                .isBreakNestedChain(breakNestedChain)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractTestRunnable that = (AbstractTestRunnable) o;
        return isShareState() == that.isShareState() &&
                isBreakNestedChain() == that.isBreakNestedChain() &&
                isNestedChain() == that.isNestedChain() &&
                Objects.equal(getScriptFile(), that.getScriptFile()) &&
                Objects.equal(getTestDataSet(), that.getTestDataSet()) &&
                Objects.equal(getOverrideTestDataSet(), that.getOverrideTestDataSet()) &&
                Objects.equal(getSkip(), that.getSkip()) &&
                Objects.equal(getLazyLoad(), that.getLazyLoad());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getScriptFile(), getTestDataSet(), getOverrideTestDataSet(), isShareState(), getSkip(), getLazyLoad(), isBreakNestedChain(), isNestedChain());
    }

    public static abstract class AbstractBuilder<T extends TestRunnable, S extends Builder> implements Builder<T> {
        private ScriptFile scriptFile;
        private DataSource dataSource;
        private Map<String, String> dataSourceConfig;
        private boolean shareState;
        private String skip;
        private DataSource overrideDataSource;
        private Map<String, String> overrideDataSourceConfig;
        private Function<TestData, TestRunnable> lazyLoad;
        private boolean nestedChain;
        private boolean breakNestedChain;

        public AbstractBuilder(ScriptFile scriptFile) {
            this.scriptFile = scriptFile;
        }

        protected AbstractBuilder(T test) {
            this.scriptFile = test.getScriptFile();
            this.dataSource = test.getTestDataSet().getDataSource();
            this.dataSourceConfig = test.getTestDataSet().getDataSourceConfig();
            this.shareState = test.isShareState();
            this.skip = test.getSkip();
            this.overrideDataSource = test.getOverrideTestDataSet().getDataSource();
            this.overrideDataSourceConfig = test.getOverrideTestDataSet().getDataSourceConfig();
            this.lazyLoad = test.getLazyLoad();
            this.nestedChain = test.isNestedChain();
            this.breakNestedChain = test.isBreakNestedChain();
        }

        @Override
        public S setScriptFile(ScriptFile scriptFile) {
            this.scriptFile = scriptFile;
            return this.self();
        }

        @Override
        public S setName(String newName) {
            this.scriptFile = this.scriptFile.changeName(newName);
            return this.self();
        }

        @Override
        public S setDataSource(DataSource dataSource, Map<String, String> config) {
            this.dataSource = dataSource;
            this.dataSourceConfig = config;
            return this.self();
        }

        @Override
        public S addDataSourceConfig(String key, String value) {
            this.dataSourceConfig.put(key, value);
            return this.self();
        }

        @Override
        public S isShareState(boolean shareState) {
            this.shareState = shareState;
            return this.self();
        }

        @Override
        public S setSkip(String skip) {
            this.skip = skip;
            return this.self();
        }

        @Override
        public S setOverrideTestDataSet(DataSource dataSource, Map<String, String> config) {
            this.overrideDataSource = dataSource;
            this.overrideDataSourceConfig = config;
            return this.self();
        }

        @Override
        public S setLazyLoad(Function<TestData, TestRunnable> lazyLoad) {
            this.lazyLoad = lazyLoad;
            return this.self();
        }

        @Override
        public S isNestedChain(boolean nestedChain) {
            this.nestedChain = nestedChain;
            return this.self();
        }

        @Override
        public S isBreakNestedChain(boolean breakNestedChain) {
            this.breakNestedChain = breakNestedChain;
            return this.self();
        }

        @Override
        public ScriptFile getScriptFile() {
            return this.scriptFile;
        }

        @Override
        public TestDataSet getTestDataSet() {
            return new TestDataSet(this.dataSource, this.dataSourceConfig, this.scriptFile.relativePath());
        }

        @Override
        public boolean isShareState() {
            return this.shareState;
        }

        @Override
        public String getSkip() {
            return this.skip;
        }

        @Override
        public TestDataSet getOverrideTestDataSet() {
            return new TestDataSet(this.overrideDataSource, this.overrideDataSourceConfig, this.scriptFile.relativePath());
        }

        @Override
        public Function<TestData, TestRunnable> getLazyLoad() {
            return this.lazyLoad;
        }

        @Override
        public boolean isNestedChain() {
            return this.nestedChain;
        }

        @Override
        public boolean isBreakNestedChain() {
            return this.breakNestedChain;
        }

        abstract protected S self();

    }
}
