package com.sebuilder.interpreter;

import com.google.common.base.Objects;

import java.util.Iterator;

public class Suite extends AbstractTestRunnable<Suite> implements Iterable<TestCase> {

    public static final String DEFAULT_NAME = "New_Suite";

    private final Scenario scenario;

    public Suite(SuiteBuilder builder) {
        super(builder);
        this.scenario = builder.getScenario();
    }

    @Override
    public Iterator<TestCase> iterator() {
        return this.scenario.testCaseIterator();
    }

    @Override
    public TestCase head() {
        return iterator().next();
    }

    @Override
    public Suite toSuite() {
        return this;
    }

    @Override
    public TestRunBuilder[] createTestRunBuilder() {
        return this.createTestRunBuilder(this.scenario);
    }

    @Override
    public TestRunBuilder[] createTestRunBuilder(Scenario aScenario) {
        final String suiteName = this.getScriptFile().nameExcludeExtention();
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
                    return aScenario.getTestRuns(newRow, (TestRunBuilder result) -> result.addTestRunNamePrefix(prefix + "_"));
                })
                .toArray(TestRunBuilder[]::new);
    }

    @Override
    public SuiteBuilder builder() {
        return new SuiteBuilder(this);
    }

    public Scenario getScenario() {
        return this.scenario;
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

    public Suite insert(TestCase aTestCase, TestCase newTestCase) {
        return builder()
                .insertTest(aTestCase, newTestCase)
                .build();
    }

    public Suite add(TestCase aTestCase, TestCase newTestCase) {
        return builder()
                .addTest(aTestCase, newTestCase)
                .build();
    }

    public Suite add(TestCase aTestCase) {
        return builder()
                .addTest(aTestCase)
                .build();
    }

    public Suite delete(TestCase aTestCase) {
        return builder()
                .removeTest(aTestCase)
                .build();
    }

    public Suite replace(TestRunnable aTestCase) {
        return this.replace(aTestCase.name(), aTestCase);
    }

    public Suite replace(String oldName, TestRunnable newValue) {
        return builder()
                .replace(oldName, newValue)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Suite testCases = (Suite) o;
        return Objects.equal(getScenario(), testCases.getScenario());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getScenario());
    }
}
