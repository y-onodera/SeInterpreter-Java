package com.sebuilder.interpreter;

import com.google.common.base.Objects;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Suite implements Iterable<TestCase>, TestRunnable {

    public static final String DEFAULT_NAME = "New_Suite";

    private final ScriptFile scriptFile;

    private final Scenario scenario;

    private final TestDataSet testDataSet;

    private final boolean shareState;

    public Suite(File suiteFile
            , Scenario scenario
            , DataSource dataSource
            , Map<String, String> config
            , boolean shareState) {
        this.scriptFile = ScriptFile.of(suiteFile, DEFAULT_NAME);
        this.shareState = shareState;
        this.scenario = scenario;
        this.testDataSet = new TestDataSet(dataSource, config, this.getRelativePath());
    }

    @Override
    public void accept(TestRunner runner, TestRunListener testListener) {
        runner.execute(this, testListener);
    }

    @Override
    public Iterator<TestCase> iterator() {
        return this.scenario.testCaseIterator();
    }

    public List<TestData> loadData() {
        return this.testDataSet.loadData();
    }

    public ScriptFile getScriptFile() {
        return scriptFile;
    }

    public String getPath() {
        return this.scriptFile.path();
    }

    public String getName() {
        return this.scriptFile.name();
    }

    public File getRelativePath() {
        return this.scriptFile.relativePath();
    }

    public int scriptSize() {
        return this.scenario.testCaseSize();
    }

    public int getIndex(TestCase testCase) {
        return this.scenario.indexOf(testCase);
    }

    public TestCase get(String scriptName) {
        return this.scenario.get(scriptName);
    }

    public TestCase get(int index) {
        return this.scenario.get(index);
    }

    public Scenario getScenario() {
        return this.scenario;
    }

    public boolean isShareState() {
        return this.shareState;
    }

    public TestDataSet getTestDataSet() {
        return this.testDataSet;
    }

    public Map<String, String> getDataSourceConfig() {
        return this.testDataSet.getDataSourceConfig();
    }

    public DataSource getDataSource() {
        return this.testDataSet.getDataSource();
    }

    public List<TestRunBuilder> getTestRuns() {
        final String suiteName = this.scriptFile.nameExcludeExtention();
        return this.loadData()
                .stream()
                .flatMap(it -> {
                    String rowNum = it.rowNumber();
                    TestData newRow = it.clearRowNumber();
                    final String prefix;
                    if (rowNum != null) {
                        prefix = suiteName + "_" + rowNum;
                    } else {
                        prefix = suiteName;
                    }
                    return this.scenario.getTestRuns(newRow, (TestRunBuilder result) -> result.addTestRunNamePrefix(prefix + "_"));
                })
                .collect(Collectors.toList());
    }

    public SuiteBuilder builder() {
        return new SuiteBuilder(this);
    }

    public Suite skip(String skip) {
        return builder()
                .skip(skip)
                .createSuite();
    }

    public Suite insert(TestCase aTestCase, TestCase newTestCase) {
        return builder().insertTest(aTestCase, newTestCase)
                .createSuite();
    }

    public Suite add(TestCase aTestCase, TestCase newTestCase) {
        return builder().addTest(aTestCase, newTestCase)
                .createSuite();
    }

    public Suite add(TestCase aTestCase) {
        return builder()
                .addTest(aTestCase)
                .createSuite();
    }

    public Suite delete(TestCase aTestCase) {
        return builder()
                .removeTest(aTestCase)
                .createSuite();
    }

    public Suite replace(TestCase aTestCase) {
        return this.replace(aTestCase.name(), aTestCase);
    }

    public Suite replace(String oldName, TestCase newValue) {
        return builder()
                .replace(oldName, newValue)
                .createSuite();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Suite testCases = (Suite) o;
        return isShareState() == testCases.isShareState() &&
                Objects.equal(getScriptFile(), testCases.getScriptFile()) &&
                Objects.equal(getScenario(), testCases.getScenario()) &&
                Objects.equal(testDataSet, testCases.testDataSet);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getScriptFile(), getScenario(), testDataSet, isShareState());
    }

}
