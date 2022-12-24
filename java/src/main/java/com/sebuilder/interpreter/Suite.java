package com.sebuilder.interpreter;

import java.util.function.Function;
import java.util.function.Predicate;

public record Suite(TestCase head) {

    public String name() {
        return this.head().name();
    }

    public String path() {
        return this.head().path();
    }

    public TestCaseChains getChains() {
        return this.head().chains();
    }

    public TestCase get(String scriptName) {
        if (this.head().name().equals(scriptName)) {
            return this.head();
        }
        return this.head().chains().get(scriptName);
    }

    public TestCase get(int index) {
        return this.head().chains().get(index);
    }

    public DataSourceLoader[] dataSources(Predicate<TestCase> predicate) {
        return this.head.flattenTestCases()
                .filter(predicate)
                .map(TestCase::runtimeDataSet)
                .filter(dataSourceLoader -> dataSourceLoader.dataSource().enableMultiLine())
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

}
