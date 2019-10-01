package com.sebuilder.interpreter;

import java.io.File;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class TestCaseAssert {
    private Consumer<TestCase> assertFileAttribute;
    private Consumer<TestCase> assertStep;
    private Consumer<TestCase> assertDataSource;
    private Consumer<TestCase> assertSkip;
    private Consumer<TestCase> assertOverrideDataSource;
    private Consumer<TestCase> assertLazy;
    private Consumer<TestCase> assertNestedChain;

    public TestCaseAssert(Builder aBuilder) {
        this.assertFileAttribute = aBuilder.assertFileAttribute;
        this.assertStep = aBuilder.assertStep;
        this.assertDataSource = aBuilder.assertDataSource;
        this.assertSkip = aBuilder.assertSkip;
        this.assertOverrideDataSource = aBuilder.assertOverrideDataSource;
        this.assertLazy = aBuilder.assertLazy;
        this.assertNestedChain = aBuilder.assertNestedChain;
    }

    public static Builder of() {
        return new Builder();
    }

    public static Consumer<TestCase> assertEqualsFileAttribute(File testFile) {
        return (TestCase actual) -> {
            assertEquals(testFile.getName(), actual.name());
            assertEquals(testFile.getParentFile(), actual.relativePath());
        };
    }

    public static Consumer<TestCase> assertEqualsFileAttribute(String aName) {
        return (TestCase actual) -> {
            assertEquals(aName, actual.name());
            assertNull(actual.relativePath());
        };
    }

    public static Consumer<TestCase> assertEqualsFileAttribute(String aName, File testFile) {
        return (TestCase actual) -> {
            assertEquals(aName, actual.name());
            assertEquals(testFile.getParentFile(), actual.relativePath());
        };
    }

    public static void assertEqualsNoDataSource(TestCase actual) {
        assertNull(actual.getTestDataSet().getDataSource());
        assertEquals(0, actual.getTestDataSet().getDataSourceConfig().size());
    }

    public static Consumer<TestCase> assertEqualsDataSet(TestDataSet dataSource) {
        return (TestCase actual) -> assertEquals(dataSource, actual.getTestDataSet());
    }

    public static void assertEqualsNoOverrideDataSource(TestCase actual) {
        assertNull(actual.getOverrideTestDataSet().getDataSource());
        assertEquals(0, actual.getOverrideTestDataSet().getDataSourceConfig().size());
    }

    public static void assertEqualsNoStep(TestCase actual) {
        assertEquals(0, actual.steps().size());
    }

    public static Consumer<TestCase> assertEqualsStepCount(int i) {
        return (TestCase actual) -> assertEquals(i, actual.steps().size());
    }

    public static Consumer<TestCase> assertEqualsOverrideDataSst(TestDataSet testDataSet) {
        return (TestCase actual) -> assertEquals(testDataSet, actual.getOverrideTestDataSet());
    }

    public static Consumer<TestCase> assertEqualsSkip(String aSkip) {
        return (TestCase actual) -> assertEquals(aSkip, actual.getSkip());
    }

    public static void assertLazyLoad(TestCase actual) {
        assertTrue(actual.isLazyLoad());
    }

    public static void assertNotLazyLoad(TestCase actual) {
        assertFalse(actual.isLazyLoad());
    }

    public static void assertNestedChain(TestCase actual) {
        assertTrue(actual.isNestedChain());
    }

    public static void assertNotNestedChain(TestCase actual) {
        assertFalse(actual.isNestedChain());
    }

    public void run(TestCase target) {
        this.assertFileAttribute.accept(target);
        this.assertStep.accept(target);
        this.assertDataSource.accept(target);
        this.assertSkip.accept(target);
        this.assertOverrideDataSource.accept(target);
        this.assertLazy.accept(target);
        this.assertNestedChain.accept(target);
    }

    public Builder builder() {
        return new Builder(this);
    }

    public static class Builder {
        private Consumer<TestCase> assertFileAttribute;
        private Consumer<TestCase> assertStep;
        private Consumer<TestCase> assertDataSource;
        private Consumer<TestCase> assertSkip;
        private Consumer<TestCase> assertOverrideDataSource;
        private Consumer<TestCase> assertLazy;
        public Consumer<TestCase> assertNestedChain;

        public Builder() {
        }

        public Builder(TestCaseAssert testCaseAssert) {
            this.assertFileAttribute = testCaseAssert.assertFileAttribute;
            this.assertStep = testCaseAssert.assertStep;
            this.assertDataSource = testCaseAssert.assertDataSource;
            this.assertSkip = testCaseAssert.assertSkip;
            this.assertOverrideDataSource = testCaseAssert.assertOverrideDataSource;
            this.assertLazy = testCaseAssert.assertLazy;
            this.assertNestedChain = testCaseAssert.assertNestedChain;
        }

        public Builder assertFileAttribute(Consumer<TestCase> assertion) {
            this.assertFileAttribute = assertion;
            return this;
        }

        public Builder assertStep(Consumer<TestCase> assertion) {
            this.assertStep = assertion;
            return this;
        }

        public Builder assertDataSource(Consumer<TestCase> assertion) {
            this.assertDataSource = assertion;
            return this;
        }

        public Builder assertSkip(Consumer<TestCase> assertion) {
            this.assertSkip = assertion;
            return this;
        }

        public Builder assertOverrideDataSource(Consumer<TestCase> assertion) {
            this.assertOverrideDataSource = assertion;
            return this;
        }

        public Builder assertLazy(Consumer<TestCase> assertion) {
            this.assertLazy = assertion;
            return this;
        }

        public Builder assertNestedChain(Consumer<TestCase> assertion) {
            this.assertNestedChain = assertion;
            return this;
        }

        public TestCaseAssert create() {
            return new TestCaseAssert(this);
        }
    }

}
