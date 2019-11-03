package com.sebuilder.interpreter;

import com.google.common.base.Objects;

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

    public int getIndex(TestCase testCase) {
        return this.head().getChains().indexOf(testCase);
    }

    public TestCaseBuilder builder() {
        return new TestCaseBuilder(this.head());
    }

    public Suite insert(TestCase aTestCase, TestCase newTestCase) {
        return builder()
                .insertTest(aTestCase, newTestCase)
                .build()
                .toSuite();
    }

    public Suite add(TestCase aTestCase, TestCase newTestCase) {
        return builder()
                .addChain(aTestCase, newTestCase)
                .build()
                .toSuite();
    }

    public Suite add(TestCase aTestCase) {
        return builder()
                .addChain(aTestCase)
                .build()
                .toSuite();
    }

    public Suite delete(TestCase aTestCase) {
        return builder()
                .remove(aTestCase)
                .build()
                .toSuite();
    }

    public Suite replace(TestCase oldCase, TestCase newValue) {
        if (this.head.equals(oldCase)) {
            return newValue.toSuite();
        }
        return builder()
                .replace(oldCase, newValue)
                .build()
                .toSuite();
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
