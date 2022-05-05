package com.sebuilder.interpreter.script;

import com.google.common.collect.Maps;
import com.sebuilder.interpreter.DataSource;
import com.sebuilder.interpreter.DataSourceLoader;
import com.sebuilder.interpreter.ScriptFile;
import com.sebuilder.interpreter.TestCase;
import org.junit.Assert;

import java.io.File;
import java.util.Map;
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
    private Consumer<TestCase> assertChainCaseCounts;
    private Map<Integer, TestCaseAssert> assertChainCases = Maps.newHashMap();

    public TestCaseAssert(Builder aBuilder) {
        this.assertFileAttribute = aBuilder.assertFileAttribute;
        this.assertStep = aBuilder.assertStep;
        this.assertDataSource = aBuilder.assertDataSource;
        this.assertSkip = aBuilder.assertSkip;
        this.assertOverrideDataSource = aBuilder.assertOverrideDataSource;
        this.assertLazy = aBuilder.assertLazy;
        this.assertNestedChain = aBuilder.assertNestedChain;
        this.assertChainCaseCounts = aBuilder.assertChainCaseCounts;
        this.assertChainCases.putAll(aBuilder.assertChainCases);
    }

    public static Builder of() {
        return new Builder();
    }

    public static void assertEqualsNoRelationFile(TestCase result) {
        Assert.assertEquals(ScriptFile.Type.TEST.getDefaultName(), result.name());
        assertEquals("", result.path());
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
        assertSame(DataSource.NONE, actual.getDataSourceLoader().getDataSource());
        assertEquals(0, actual.getDataSourceLoader().getDataSourceConfig().size());
    }

    public static Consumer<TestCase> assertEqualsDataSet(DataSourceLoader dataSource) {
        return (TestCase actual) -> assertEquals(dataSource, actual.getDataSourceLoader());
    }

    public static void assertEqualsNoChainCase(TestCase actual) {
        assertEquals(0, actual.getChains().size());
    }

    public static Consumer<TestCase> assertEqualsChainCaseCount(int count) {
        return (TestCase actual) -> assertEquals(count, actual.getChains().size());
    }

    public static void assertEqualsNoStep(TestCase actual) {
        assertEquals(0, actual.steps().size());
    }

    public static Consumer<TestCase> assertEqualsStepCount(int i) {
        return (TestCase actual) -> assertEquals(i, actual.steps().size());
    }

    public static void assertEqualsNoOverrideDataSource(TestCase actual) {
        assertSame(DataSource.NONE, actual.getOverrideDataSourceLoader().getDataSource());
        assertEquals(0, actual.getOverrideDataSourceLoader().getDataSourceConfig().size());
    }

    public static Consumer<TestCase> assertEqualsOverrideDataSst(DataSourceLoader dataSourceLoader) {
        return (TestCase actual) -> assertEquals(dataSourceLoader, actual.getOverrideDataSourceLoader());
    }

    public static Consumer<TestCase> assertEqualsNoSkip() {
        return assertEqualsSkip("false");
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
        this.assertChainCaseCounts.accept(target);
        this.assertChainCases.entrySet()
                .forEach(entry -> entry.getValue().run(target.getChains().get(entry.getKey())));
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
        private Consumer<TestCase> assertNestedChain;
        private Consumer<TestCase> assertChainCaseCounts;
        private Map<Integer, TestCaseAssert> assertChainCases = Maps.newHashMap();

        public Builder() {
            this.assertFileAttribute = TestCaseAssert::assertEqualsNoRelationFile;
            this.assertStep = TestCaseAssert::assertEqualsNoStep;
            this.assertDataSource = TestCaseAssert::assertEqualsNoDataSource;
            this.assertSkip = TestCaseAssert.assertEqualsNoSkip();
            this.assertOverrideDataSource = TestCaseAssert::assertEqualsNoOverrideDataSource;
            this.assertLazy = TestCaseAssert::assertNotLazyLoad;
            this.assertNestedChain = TestCaseAssert::assertNotNestedChain;
            this.assertChainCaseCounts = TestCaseAssert::assertEqualsNoChainCase;
        }

        public Builder(TestCaseAssert testCaseAssert) {
            this.assertFileAttribute = testCaseAssert.assertFileAttribute;
            this.assertStep = testCaseAssert.assertStep;
            this.assertDataSource = testCaseAssert.assertDataSource;
            this.assertSkip = testCaseAssert.assertSkip;
            this.assertOverrideDataSource = testCaseAssert.assertOverrideDataSource;
            this.assertLazy = testCaseAssert.assertLazy;
            this.assertNestedChain = testCaseAssert.assertNestedChain;
            this.assertChainCaseCounts = testCaseAssert.assertChainCaseCounts;
            this.assertChainCases.putAll(testCaseAssert.assertChainCases);
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

        public Builder assertChainCaseCounts(int i) {
            return this.assertChainCaseCounts(TestCaseAssert.assertEqualsChainCaseCount(i));
        }

        public Builder assertChainCaseCounts(Consumer<TestCase> assertion) {
            this.assertChainCaseCounts = assertion;
            return this;
        }

        public Builder assertChainCase(int i, TestCaseAssert testCaseAssert) {
            this.assertChainCases.put(i, testCaseAssert);
            return this;
        }

        public TestCaseAssert create() {
            return new TestCaseAssert(this);
        }

    }

}
