package com.sebuilder.interpreter;

import com.google.common.base.Predicates;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.function.Function;

public class TestCaseBuilder {

    private ScriptFile scriptFile;
    private DataSource dataSource;
    private Map<String, String> dataSourceConfig;
    private TestData shareInput;
    private boolean shareState;
    private String skip;
    private DataSource overrideDataSource;
    private Map<String, String> overrideDataSourceConfig;
    private Function<TestData, TestCase> lazyLoad;
    private boolean nestedChain;
    private boolean breakNestedChain;
    private TestCaseChains chains;
    private Aspect aspect;
    private Function<TestCase, TestCase> converter = script -> {
        if (script.isShareState() != this.isShareState()) {
            return script.map(it -> it.isShareState(this.isShareState()));
        }
        return script;
    };
    private ArrayList<Step> steps;

    public TestCaseBuilder() {
        this(new ScriptFile(ScriptFile.Type.TEST));
    }

    public TestCaseBuilder(ScriptFile scriptFile) {
        this.scriptFile = scriptFile;
        this.chains = new TestCaseChains();
        this.aspect = new Aspect();
        this.skip = "false";
        this.steps = new ArrayList<>();
    }


    public TestCaseBuilder(TestCase test) {
        this.scriptFile = test.getScriptFile();
        this.shareInput = test.getShareInput();
        this.dataSource = test.getTestDataSet().getDataSource();
        this.dataSourceConfig = test.getTestDataSet().getDataSourceConfig();
        this.shareState = test.isShareState();
        this.skip = test.getSkip();
        this.overrideDataSource = test.getOverrideTestDataSet().getDataSource();
        this.overrideDataSourceConfig = test.getOverrideTestDataSet().getDataSourceConfig();
        this.lazyLoad = test.getLazyLoad();
        this.nestedChain = test.isNestedChain();
        this.breakNestedChain = test.isBreakNestedChain();
        this.chains = test.getChains();
        this.aspect = test.getAspect();
        this.steps = new ArrayList<>(test.steps());
    }

    public static TestCaseBuilder suite(File suiteFile) {
        return new TestCaseBuilder(ScriptFile.of(suiteFile, ScriptFile.Type.SUITE))
                .isShareState(true);
    }

    public static TestCase lazyLoad(String beforeReplace, Function<TestData, TestCase> lazyLoad) {
        return new TestCaseBuilder()
                .setName(beforeReplace)
                .setLazyLoad(lazyLoad)
                .build();
    }

    public TestCase build() {
        return new TestCase(this);
    }

    public TestCaseBuilder setScriptFile(ScriptFile scriptFile) {
        this.scriptFile = scriptFile;
        return this;
    }

    public TestCaseBuilder setName(String newName) {
        this.scriptFile = this.scriptFile.changeName(newName);
        return this;
    }

    public TestCaseBuilder setShareInput(TestData testData) {
        this.shareInput = testData;
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

    public TestCaseBuilder isShareState(boolean shareState) {
        this.shareState = shareState;
        return this;
    }

    public TestCaseBuilder setSkip(String skip) {
        this.skip = skip;
        return this;
    }

    public TestCaseBuilder setOverrideTestDataSet(DataSource dataSource, Map<String, String> config) {
        this.overrideDataSource = dataSource;
        this.overrideDataSourceConfig = config;
        return this;
    }

    public TestCaseBuilder setLazyLoad(Function<TestData, TestCase> lazyLoad) {
        this.lazyLoad = lazyLoad;
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

    public TestCaseBuilder isChainTakeOverLastRun(boolean b) {
        this.chains = this.chains.takeOverLastRun(b);
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
        return addChain(newTestCase, index);
    }

    public TestCaseBuilder insertTest(TestCase aTestCase, TestCase newTestCase) {
        final int index = this.chains.indexOf(aTestCase);
        return addChain(newTestCase, index);
    }

    public TestCaseBuilder addChain(TestCase newTestCase, int index) {
        this.chains = this.chains.append(index, newTestCase);
        return this;
    }

    public TestCaseBuilder remove(TestCase aTestCase) {
        this.chains = this.chains.remove(aTestCase);
        return this;
    }

    public TestCaseBuilder replace(TestCase oldCase, TestCase aTestCase) {
        this.chains = this.chains.replaceTest(oldCase, aTestCase);
        return this;
    }

    public TestCaseBuilder addAspect(Aspect aspect) {
        this.aspect = this.aspect.builder()
                .add(aspect)
                .build();
        return this;
    }

    public TestCaseBuilder setAspect(Aspect aspect) {
        this.aspect = aspect;
        return this;
    }

    public ScriptFile getScriptFile() {
        return this.scriptFile;
    }

    public TestData getShareInput() {
        return this.shareInput;
    }

    public TestDataSet getTestDataSet() {
        return new TestDataSet(this.dataSource, this.dataSourceConfig, this.scriptFile.relativePath());
    }

    public boolean isShareState() {
        return this.shareState;
    }

    public String getSkip() {
        return this.skip;
    }

    public TestDataSet getOverrideTestDataSet() {
        return new TestDataSet(this.overrideDataSource, this.overrideDataSourceConfig, this.scriptFile.relativePath());
    }

    public Function<TestData, TestCase> getLazyLoad() {
        return this.lazyLoad;
    }

    public boolean isNestedChain() {
        return this.nestedChain;
    }

    public boolean isBreakNestedChain() {
        return this.breakNestedChain;
    }

    public TestCaseChains getChains() {
        return this.chains.map(this.converter, Predicates.alwaysTrue());
    }

    public Aspect getAspect() {
        return this.aspect;
    }

    public TestCaseBuilder associateWith(File target) {
        this.setScriptFile(ScriptFile.of(target, this.getScriptFile().type()));
        return this;
    }

    public ArrayList<Step> getSteps() {
        return this.steps;
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

}