package com.sebuilder.interpreter;

import com.google.common.base.Objects;

import java.util.function.Function;

public class Suite {

    private final TestCase head;

    public Suite(TestCase head) {
        this.head = head;
    }

    public String name() {
        return this.head().name();
    }

    public String path() {
        return this.head().path();
    }

    public TestCase head() {
        return this.head;
    }

    public TestCaseChains getChains() {
        return this.head().getChains();
    }

    public TestCase get(String scriptName) {
        if (this.head().name().equals(scriptName)) {
            return this.head();
        }
        return this.head().getChains().get(scriptName);
    }

    public TestCase get(int index) {
        return this.head().getChains().get(index);
    }

    public DataSourceLoader[] dataSources(TestCase target) {
        return this.head.flattenTestCases()
                .filter(it -> it.include(target))
                .filter(it -> it.runtimeDataSet().getDataSource().enableMultiLine())
                .map(TestCase::runtimeDataSet)
                .toArray(DataSourceLoader[]::new);
    }

    public TestCaseBuilder builder() {
        return new TestCaseBuilder(this.head());
    }

    public Suite map(Function<TestCaseBuilder, TestCaseBuilder> function) {
        return function.apply(this.builder()).build().toSuite();
    }

    public Suite replace(TestCase oldCase, TestCase newValue) {
        if (this.head.equals(oldCase)) {
            return newValue.toSuite();
        }
        return map(builder -> builder.replace(oldCase, newValue));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Suite suite = (Suite) o;
        return Objects.equal(head, suite.head);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(head);
    }

}
