package com.sebuilder.interpreter;

import com.google.common.base.Objects;

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

    public DataSourceLoader[] dataSources(Predicate<TestCase> predicate) {
        return this.head.flattenTestCases()
                .filter(predicate)
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

}
