package com.sebuilder.interpreter.script;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.*;
import org.junit.Assert;

import java.io.File;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.*;

public class TestCaseAssert {
    private final Consumer<TestCase> assertFileAttribute;
    private final Consumer<TestCase> assertStep;
    private final Consumer<TestCase> assertDataSource;
    private final Consumer<TestCase> assertSkip;
    private final Consumer<TestCase> assertOverrideDataSource;
    private final Consumer<Pointcut> assertIncludeTestRun;
    private final Consumer<Pointcut> assertExcludeTestRun;
    private final Consumer<TestCase> assertLazy;
    private final Consumer<TestCase> assertNestedChain;
    private final Consumer<TestCase> assertChainCaseCounts;
    private final Map<Integer, TestCaseAssert> assertChainCases = Maps.newHashMap();

    public TestCaseAssert(final Builder aBuilder) {
        this.assertFileAttribute = aBuilder.assertFileAttribute;
        this.assertStep = aBuilder.assertStep;
        this.assertDataSource = aBuilder.assertDataSource;
        this.assertSkip = aBuilder.assertSkip;
        this.assertOverrideDataSource = aBuilder.assertOverrideDataSource;
        this.assertIncludeTestRun = aBuilder.assertIncludeTestRun;
        this.assertExcludeTestRun = aBuilder.assertExcludeTestRun;
        this.assertLazy = aBuilder.assertLazy;
        this.assertNestedChain = aBuilder.assertNestedChain;
        this.assertChainCaseCounts = aBuilder.assertChainCaseCounts;
        this.assertChainCases.putAll(aBuilder.assertChainCases);
    }

    public static Builder of() {
        return new Builder();
    }

    public static void assertEqualsNoRelationFile(final TestCase result) {
        Assert.assertEquals(ScriptFile.Type.TEST.getDefaultName(), result.name());
        assertEquals("", result.path());
    }

    public static Consumer<TestCase> assertEqualsFileAttribute(final File testFile) {
        return (TestCase actual) -> {
            assertEquals(testFile.getName(), actual.name());
            assertEquals(testFile.getParentFile(), actual.relativePath());
        };
    }

    public static Consumer<TestCase> assertEqualsFileAttribute(final String aName) {
        return (TestCase actual) -> {
            assertEquals(aName, actual.name());
            assertNull(actual.relativePath());
        };
    }

    public static Consumer<TestCase> assertEqualsFileAttribute(final String aName, final File testFile) {
        return (TestCase actual) -> {
            assertEquals(aName, actual.name());
            assertEquals(testFile.getParentFile(), actual.relativePath());
        };
    }

    public static void assertEqualsNoDataSource(final TestCase actual) {
        assertSame(DataSource.NONE, actual.dataSourceLoader().dataSource());
        assertEquals(0, actual.dataSourceLoader().dataSourceConfig().size());
    }

    public static Consumer<TestCase> assertEqualsDataSet(final DataSourceLoader dataSource) {
        return (TestCase actual) -> assertEquals(dataSource, actual.dataSourceLoader());
    }

    public static void assertEqualsNoChainCase(final TestCase actual) {
        assertEquals(0, actual.chains().size());
    }

    public static Consumer<TestCase> assertEqualsChainCaseCount(final int count) {
        return (TestCase actual) -> assertEquals(count, actual.chains().size());
    }

    public static void assertEqualsNoStep(final TestCase actual) {
        assertEquals(0, actual.steps().size());
    }

    public static Consumer<TestCase> assertEqualsStepCount(final int i) {
        return (TestCase actual) -> assertEquals(i, actual.steps().size());
    }

    public static void assertEqualsNoOverrideDataSource(final TestCase actual) {
        assertSame(DataSource.NONE, actual.overrideDataSourceLoader().dataSource());
        assertEquals(0, actual.overrideDataSourceLoader().dataSourceConfig().size());
    }

    public static Consumer<TestCase> assertEqualsOverrideDataSst(final DataSourceLoader dataSourceLoader) {
        return (TestCase actual) -> assertEquals(dataSourceLoader, actual.overrideDataSourceLoader());
    }

    public static Consumer<TestCase> assertEqualsNoSkip() {
        return assertEqualsSkip("false");
    }

    public static Consumer<TestCase> assertEqualsSkip(final String aSkip) {
        return (TestCase actual) -> assertEquals(aSkip, actual.skip());
    }

    public static void assertLazyLoad(final TestCase actual) {
        assertTrue(actual.isLazyLoad());
    }

    public static void assertNotLazyLoad(final TestCase actual) {
        assertFalse(actual.isLazyLoad());
    }

    public static void assertNestedChain(final TestCase actual) {
        assertTrue(actual.nestedChain());
    }

    public static void assertNotNestedChain(final TestCase actual) {
        assertFalse(actual.nestedChain());
    }

    public void run(final TestCase target) {
        this.assertFileAttribute.accept(target);
        this.assertStep.accept(target);
        this.assertDataSource.accept(target);
        this.assertSkip.accept(target);
        this.assertOverrideDataSource.accept(target);
        this.assertIncludeTestRun.accept(target.includeTestRun());
        this.assertExcludeTestRun.accept(target.excludeTestRun());
        this.assertLazy.accept(target);
        this.assertNestedChain.accept(target);
        this.assertChainCaseCounts.accept(target);
        this.assertChainCases.forEach((key, value) -> value.run(target.chains().get(key)));
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
        private Consumer<Pointcut> assertIncludeTestRun;
        private Consumer<Pointcut> assertExcludeTestRun;
        private Consumer<TestCase> assertLazy;
        private Consumer<TestCase> assertNestedChain;
        private Consumer<TestCase> assertChainCaseCounts;
        private final Map<Integer, TestCaseAssert> assertChainCases = Maps.newHashMap();

        public Builder() {
            this.assertFileAttribute = TestCaseAssert::assertEqualsNoRelationFile;
            this.assertStep = TestCaseAssert::assertEqualsNoStep;
            this.assertDataSource = TestCaseAssert::assertEqualsNoDataSource;
            this.assertSkip = TestCaseAssert.assertEqualsNoSkip();
            this.assertOverrideDataSource = TestCaseAssert::assertEqualsNoOverrideDataSource;
            this.assertIncludeTestRun = it -> assertSame(Pointcut.ANY, it);
            this.assertExcludeTestRun = it -> assertSame(Pointcut.NONE, it);
            this.assertLazy = TestCaseAssert::assertNotLazyLoad;
            this.assertNestedChain = TestCaseAssert::assertNotNestedChain;
            this.assertChainCaseCounts = TestCaseAssert::assertEqualsNoChainCase;
        }

        public Builder(final TestCaseAssert testCaseAssert) {
            this.assertFileAttribute = testCaseAssert.assertFileAttribute;
            this.assertStep = testCaseAssert.assertStep;
            this.assertDataSource = testCaseAssert.assertDataSource;
            this.assertSkip = testCaseAssert.assertSkip;
            this.assertOverrideDataSource = testCaseAssert.assertOverrideDataSource;
            this.assertIncludeTestRun = testCaseAssert.assertIncludeTestRun;
            this.assertExcludeTestRun = testCaseAssert.assertExcludeTestRun;
            this.assertLazy = testCaseAssert.assertLazy;
            this.assertNestedChain = testCaseAssert.assertNestedChain;
            this.assertChainCaseCounts = testCaseAssert.assertChainCaseCounts;
            this.assertChainCases.putAll(testCaseAssert.assertChainCases);
        }

        public Builder assertFileAttribute(final Consumer<TestCase> assertion) {
            this.assertFileAttribute = assertion;
            return this;
        }

        public Builder assertStep(final Consumer<TestCase> assertion) {
            this.assertStep = assertion;
            return this;
        }

        public Builder assertDataSource(final Consumer<TestCase> assertion) {
            this.assertDataSource = assertion;
            return this;
        }

        public Builder assertSkip(final Consumer<TestCase> assertion) {
            this.assertSkip = assertion;
            return this;
        }

        public Builder assertOverrideDataSource(final Consumer<TestCase> assertion) {
            this.assertOverrideDataSource = assertion;
            return this;
        }

        public Builder assertIncludeTestRun(final Consumer<Pointcut> assertion) {
            this.assertIncludeTestRun = assertion;
            return this;
        }

        public Builder assertExcludeTestRun(final Consumer<Pointcut> assertion) {
            this.assertExcludeTestRun = assertion;
            return this;
        }

        public Builder assertLazy(final Consumer<TestCase> assertion) {
            this.assertLazy = assertion;
            return this;
        }

        public Builder assertNestedChain(final Consumer<TestCase> assertion) {
            this.assertNestedChain = assertion;
            return this;
        }

        public Builder assertChainCaseCounts(final int i) {
            return this.assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(i));
        }

        public Builder assertChainCaseCounts(final Consumer<TestCase> assertion) {
            this.assertChainCaseCounts = assertion;
            return this;
        }

        public Builder assertChainCase(final int i, final TestCaseAssert testCaseAssert) {
            this.assertChainCases.put(i, testCaseAssert);
            return this;
        }

        public TestCaseAssert create() {
            return new TestCaseAssert(this);
        }

    }

}
