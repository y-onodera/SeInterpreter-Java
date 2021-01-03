package com.sebuilder.interpreter;

import com.google.common.base.Predicates;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public class TestCaseBuilder {

    private ScriptFile scriptFile;
    private ArrayList<Step> steps;
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;
    private Aspect aspect;
    private boolean shareState;
    private InputData shareInput;
    private DataSource overrideDataSource;
    private Map<String, String> overrideDataSourceConfig;
    private String skip;
    private TestCaseChains chains;
    private final Function<TestCase, TestCase> chainConverter = script -> {
        if (script.isShareState() != this.isShareState()) {
            return script.map(it -> it.isShareState(this.isShareState()));
        }
        return script;
    };
    private boolean nestedChain;
    private boolean breakNestedChain;
    private boolean preventContextAspect;
    private BiFunction<TestCase, TestRunListener, TestCase> lazyLoad;

    public TestCaseBuilder() {
        this(new ScriptFile(ScriptFile.Type.TEST));
    }

    public TestCaseBuilder(ScriptFile scriptFile) {
        this.scriptFile = scriptFile;
        this.steps = new ArrayList<>();
        this.dataSource = DataSource.NONE;
        this.aspect = new Aspect();
        this.overrideDataSource = DataSource.NONE;
        this.skip = "false";
        this.chains = new TestCaseChains();
    }

    public TestCaseBuilder(TestCase test) {
        this.scriptFile = test.getScriptFile();
        this.steps = new ArrayList<>(test.steps());
        this.dataSource = test.getDataSourceLoader().getDataSource();
        this.dataSourceConfig = test.getDataSourceLoader().getDataSourceConfig();
        this.aspect = test.getAspect();
        this.shareState = test.isShareState();
        this.shareInput = test.getShareInput();
        this.overrideDataSource = test.getOverrideDataSourceLoader().getDataSource();
        this.overrideDataSourceConfig = test.getOverrideDataSourceLoader().getDataSourceConfig();
        this.skip = test.getSkip();
        this.chains = test.getChains();
        this.nestedChain = test.isNestedChain();
        this.breakNestedChain = test.isBreakNestedChain();
        this.preventContextAspect = test.isPreventContextAspect();
        this.lazyLoad = test.getLazyLoad();
    }

    public static TestCase lazyLoad(String beforeReplace, BiFunction<TestCase, TestRunListener, TestCase> lazyLoad) {
        return new TestCaseBuilder()
                .setName(beforeReplace)
                .setLazyLoad(lazyLoad)
                .build();
    }

    public static TestCaseBuilder suite(File suiteFile) {
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
        return this.chains.map(this.chainConverter, Predicates.alwaysTrue());
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

    public BiFunction<TestCase, TestRunListener, TestCase> getLazyLoad() {
        return this.lazyLoad;
    }

    public TestCase build() {
        return new TestCase(this);
    }

    public TestCaseBuilder map(Function<TestCaseBuilder, TestCaseBuilder> function) {
        return function.apply(this);
    }

    public TestCaseBuilder changeWhenConditionMatch(Predicate<TestCaseBuilder> condition, Function<TestCaseBuilder, TestCaseBuilder> function) {
        if (condition.test(this)) {
            return function.apply(this);
        }
        return this;
    }

    public TestCaseBuilder associateWith(File target) {
        this.scriptFile = ScriptFile.of(target, this.getScriptFile().type());
        return this;
    }

    public TestCaseBuilder setName(String newName) {
        this.scriptFile = this.scriptFile.changeName(newName);
        return this;
    }

    public TestCaseBuilder clearStep() {
        this.steps.clear();
        return this;
    }

    public TestCaseBuilder addSteps(ArrayList<Step> steps) {
        this.steps.addAll(steps);
        return this;
    }

    public TestCaseBuilder addStep(Step aStep) {
        this.steps.add(aStep);
        return this;
    }

    public TestCaseBuilder setTestDataSet(DataSourceLoader dataSourceLoader) {
        this.dataSource = dataSourceLoader.getDataSource();
        this.dataSourceConfig = dataSourceLoader.getDataSourceConfig();
        return this;
    }

    public TestCaseBuilder setDataSource(DataSource dataSource, Map<String, String> config) {
        this.dataSource = dataSource;
        this.dataSourceConfig = config;
        return this;
    }

    public TestCaseBuilder addDataSourceConfig(String key, String value) {
        this.dataSourceConfig.put(key, value);
        return this;
    }

    public TestCaseBuilder setAspect(Aspect aspect) {
        this.aspect = aspect;
        return this;
    }

    public TestCaseBuilder addAspect(Aspect aspect) {
        this.aspect = this.aspect.builder()
                .add(aspect)
                .build();
        return this;
    }

    public TestCaseBuilder isShareState(boolean shareState) {
        this.shareState = shareState;
        return this;
    }

    public TestCaseBuilder setShareInput(InputData inputData) {
        this.shareInput = inputData;
        return this;
    }

    public TestCaseBuilder setOverrideTestDataSet(DataSource dataSource, Map<String, String> config) {
        this.overrideDataSource = dataSource;
        this.overrideDataSourceConfig = config;
        return this;
    }

    public TestCaseBuilder setSkip(String skip) {
        this.skip = skip;
        return this;
    }

    public TestCaseBuilder setChains(TestCaseChains testCaseChain) {
        this.chains = testCaseChain;
        return this;
    }

    public TestCaseBuilder addChain(TestCase s) {
        this.chains = this.chains.append(s);
        return this;
    }

    public TestCaseBuilder addChain(TestCase aTestCase, TestCase newTestCase) {
        final int index = this.chains.indexOf(aTestCase) + 1;
        return this.addChain(newTestCase, index);
    }

    public TestCaseBuilder addChain(TestCase newTestCase, int index) {
        this.chains = this.chains.append(index, newTestCase);
        return this;
    }

    public TestCaseBuilder isChainTakeOverLastRun(boolean b) {
        this.chains = this.chains.takeOverLastRun(b);
        return this;
    }

    public TestCaseBuilder insertTest(TestCase aTestCase, TestCase newTestCase) {
        final int index = this.chains.indexOf(aTestCase);
        return this.addChain(newTestCase, index);
    }

    public TestCaseBuilder remove(TestCase aTestCase) {
        this.chains = this.chains.remove(aTestCase);
        return this;
    }

    public TestCaseBuilder replace(TestCase oldCase, TestCase aTestCase) {
        this.chains = this.chains.replaceTest(oldCase, aTestCase);
        return this;
    }

    public TestCaseBuilder isNestedChain(boolean nestedChain) {
        this.nestedChain = nestedChain;
        return this;
    }

    public TestCaseBuilder isBreakNestedChain(boolean breakNestedChain) {
        this.breakNestedChain = breakNestedChain;
        return this;
    }

    public TestCaseBuilder isPreventContextAspect(boolean preventContextAspect) {
        this.preventContextAspect = preventContextAspect;
        return this;
    }

    public TestCaseBuilder setLazyLoad(BiFunction<TestCase, TestRunListener, TestCase> lazyLoad) {
        this.lazyLoad = lazyLoad;
        return this;
    }
}