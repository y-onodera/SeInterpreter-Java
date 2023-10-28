package com.sebuilder.interpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

public class TestCaseBuilder {

    private ScriptFile scriptFile;
    private final ArrayList<Step> steps;
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;
    private boolean shareState;
    private InputData shareInput;
    private DataSource overrideDataSource;
    private Aspect aspect;
    private Pointcut includeTestRun;
    private Pointcut excludeTestRun;
    private Map<String, String> overrideDataSourceConfig;
    private String skip;
    private TestCaseChains chains;
    private final Function<TestCaseBuilder, TestCaseBuilder> chainConverter = src -> {
        if (src.isShareState() != this.isShareState()) {
            return src.isShareState(this.isShareState());
        }
        return src;
    };
    private boolean nestedChain;
    private boolean breakNestedChain;
    private boolean preventContextAspect;
    private Function<TestCase, TestCase> lazyLoad;

    public TestCaseBuilder() {
        this(new ScriptFile(ScriptFile.Type.TEST));
    }

    public TestCaseBuilder(final ScriptFile scriptFile) {
        this.scriptFile = scriptFile;
        this.steps = new ArrayList<>();
        this.dataSource = DataSource.NONE;
        this.overrideDataSource = DataSource.NONE;
        this.aspect = new Aspect();
        this.includeTestRun = Pointcut.ANY;
        this.excludeTestRun = Pointcut.NONE;
        this.skip = "false";
        this.chains = new TestCaseChains();
    }

    public TestCaseBuilder(final TestCase test) {
        this.scriptFile = test.scriptFile();
        this.steps = new ArrayList<>(test.steps());
        this.dataSource = test.dataSourceLoader().dataSource();
        this.dataSourceConfig = test.dataSourceLoader().dataSourceConfig();
        this.shareState = test.shareState();
        this.shareInput = test.shareInput();
        this.overrideDataSource = test.overrideDataSourceLoader().dataSource();
        this.overrideDataSourceConfig = test.overrideDataSourceLoader().dataSourceConfig();
        this.aspect = test.aspect();
        this.includeTestRun = test.includeTestRun();
        this.excludeTestRun = test.excludeTestRun();
        this.skip = test.skip();
        this.chains = test.chains();
        this.nestedChain = test.nestedChain();
        this.breakNestedChain = test.breakNestedChain();
        this.preventContextAspect = test.preventContextAspect();
        this.lazyLoad = test.lazyLoad();
    }

    public static TestCase lazyLoad(final String beforeReplace, final Function<TestCase, TestCase> lazyLoad) {
        return new TestCaseBuilder()
                .setName(beforeReplace)
                .setLazyLoad(lazyLoad)
                .build();
    }

    public static TestCaseBuilder suite(final File suiteFile) {
        return new TestCaseBuilder(ScriptFile.of(suiteFile, ScriptFile.Type.SUITE))
                .isShareState(true);
    }

    public ScriptFile getScriptFile() {
        return this.scriptFile;
    }

    public ArrayList<Step> getSteps() {
        return this.steps;
    }

    public DataSourceLoader getTestDataSet() {
        return new DataSourceLoader(this.dataSource, this.dataSourceConfig, this.scriptFile.relativePath());
    }

    public Aspect getAspect() {
        return this.aspect;
    }

    public Pointcut getIncludeTestRun() {
        return this.includeTestRun;
    }

    public Pointcut getExcludeTestRun() {
        return this.excludeTestRun;
    }

    public boolean isShareState() {
        return this.shareState;
    }

    public InputData getShareInput() {
        if (this.shareInput == null) {
            return new InputData();
        }
        return this.shareInput;
    }

    public DataSourceLoader getOverrideTestDataSet() {
        return new DataSourceLoader(this.overrideDataSource, this.overrideDataSourceConfig, this.scriptFile.relativePath());
    }

    public String getSkip() {
        return this.skip;
    }

    public TestCaseChains getChains() {
        return this.chains.map(this.chainConverter, testCase -> true);
    }

    public boolean isNestedChain() {
        return this.nestedChain;
    }

    public boolean isBreakNestedChain() {
        return this.breakNestedChain;
    }

    public boolean isPreventContextAspect() {
        return this.preventContextAspect;
    }

    public Function<TestCase, TestCase> getLazyLoad() {
        return this.lazyLoad;
    }

    public TestCase build() {
        return new TestCase(this);
    }

    public TestCaseBuilder map(final Function<TestCaseBuilder, TestCaseBuilder> function) {
        return function.apply(this);
    }

    public TestCaseBuilder mapWhen(final Predicate<TestCaseBuilder> condition, final Function<TestCaseBuilder, TestCaseBuilder> function) {
        if (condition.test(this)) {
            return function.apply(this);
        }
        return this;
    }

    public TestCaseBuilder mapChains(final Function<TestCaseBuilder, TestCaseBuilder> converter, final Predicate<TestCase> isNestChainConvert) {
        this.setChains(this.chains.map(converter, isNestChainConvert));
        return this;
    }

    public TestCaseBuilder associateWith(final File target) {
        this.scriptFile = ScriptFile.of(target, this.getScriptFile().type());
        return this;
    }

    public TestCaseBuilder setName(final String newName) {
        this.scriptFile = this.scriptFile.changeName(newName);
        return this;
    }

    public TestCaseBuilder clearStep() {
        this.steps.clear();
        return this;
    }

    public TestCaseBuilder addSteps(final ArrayList<Step> steps) {
        this.steps.addAll(steps);
        return this;
    }

    public TestCaseBuilder addStep(final Step aStep) {
        this.steps.add(aStep);
        return this;
    }

    public TestCaseBuilder setTestDataSet(final DataSourceLoader dataSourceLoader) {
        this.dataSource = dataSourceLoader.dataSource();
        this.dataSourceConfig = dataSourceLoader.dataSourceConfig();
        return this;
    }

    public TestCaseBuilder setDataSource(final DataSource dataSource, final Map<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
        return this;
    }

    public TestCaseBuilder addDataSourceConfig(final String key, final String value) {
        this.dataSourceConfig.put(key, value);
        return this;
    }

    public TestCaseBuilder setAspect(final Aspect aspect) {
        this.aspect = aspect;
        return this;
    }

    public TestCaseBuilder insertAspect(final Iterable<Interceptor> aspect) {
        this.aspect = this.aspect.builder()
                .insert(aspect)
                .build();
        return this;
    }

    public TestCaseBuilder addAspect(final Iterable<Interceptor> aspect) {
        this.aspect = this.aspect.builder()
                .add(aspect)
                .build();
        return this;
    }

    public TestCaseBuilder filterAspect(final Predicate<Interceptor> condition) {
        return this.setAspect(this.aspect.filter(condition));
    }

    public TestCaseBuilder setIncludeTestRun(final Pointcut includeTestRun) {
        this.includeTestRun = includeTestRun;
        return this;
    }

    public TestCaseBuilder setExcludeTestRun(final Pointcut excludeTestRun) {
        this.excludeTestRun = excludeTestRun;
        return this;
    }

    public TestCaseBuilder isShareState(final boolean shareState) {
        this.shareState = shareState;
        return this;
    }

    public TestCaseBuilder setShareInput(final InputData inputData) {
        this.shareInput = inputData;
        return this;
    }

    public TestCaseBuilder setOverrideTestDataSet(final DataSource dataSource, final Map<String, String> config) {
        this.overrideDataSource = dataSource;
        this.overrideDataSourceConfig = config;
        return this;
    }

    public TestCaseBuilder setSkip(final String skip) {
        this.skip = skip;
        return this;
    }

    public TestCaseBuilder setChains(final TestCaseChains testCaseChain) {
        this.chains = testCaseChain;
        return this;
    }

    public TestCaseBuilder addChain(final TestCase s) {
        this.chains = this.chains.append(s);
        return this;
    }

    public TestCaseBuilder addChain(final TestCase aTestCase, final TestCase newTestCase) {
        final int index = this.chains.indexOf(aTestCase) + 1;
        return this.addChain(newTestCase, index);
    }

    public TestCaseBuilder addChain(final TestCase newTestCase, final int index) {
        this.chains = this.chains.append(index, newTestCase);
        return this;
    }

    public TestCaseBuilder isChainTakeOverLastRun(final boolean b) {
        this.chains = this.chains.takeOverLastRun(b);
        return this;
    }

    public TestCaseBuilder insertTest(final TestCase aTestCase, final TestCase newTestCase) {
        final int index = this.chains.indexOf(aTestCase);
        return this.addChain(newTestCase, index);
    }

    public TestCaseBuilder remove(final TestCase aTestCase) {
        this.chains = this.chains.remove(aTestCase);
        return this;
    }

    public TestCaseBuilder replace(final TestCase oldCase, final TestCase aTestCase) {
        this.chains = this.chains.replaceTest(oldCase, aTestCase);
        return this;
    }

    public TestCaseBuilder isNestedChain(final boolean nestedChain) {
        this.nestedChain = nestedChain;
        return this;
    }

    public TestCaseBuilder isBreakNestedChain(final boolean breakNestedChain) {
        this.breakNestedChain = breakNestedChain;
        return this;
    }

    public TestCaseBuilder isPreventContextAspect(final boolean preventContextAspect) {
        this.preventContextAspect = preventContextAspect;
        return this;
    }

    public TestCaseBuilder setLazyLoad(final Function<TestCase, TestCase> lazyLoad) {
        this.lazyLoad = lazyLoad;
        return this;
    }

    public TestCaseBuilder setOverrideSetting(final TestCase copyFrom) {
        return this.setSkip(copyFrom.skip())
                .mapWhen(target -> copyFrom.overrideDataSourceLoader().dataSource() != null
                        , matches -> matches.setOverrideTestDataSet(copyFrom.overrideDataSourceLoader().dataSource()
                                , copyFrom.overrideDataSourceLoader().dataSourceConfig())
                )
                .setIncludeTestRun(copyFrom.includeTestRun())
                .setExcludeTestRun(copyFrom.excludeTestRun())
                .setAspect(copyFrom.aspect())
                .isNestedChain(copyFrom.nestedChain())
                .isBreakNestedChain(copyFrom.breakNestedChain())
                .isPreventContextAspect(copyFrom.preventContextAspect())
                ;
    }
}