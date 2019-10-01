package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.io.File;
import java.util.Collection;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SuiteAssert {

    private Consumer<Suite> assertFileAttribute;
    private Consumer<Suite> assertScenario;
    private Consumer<Suite> assertScenarioChain;
    private Map<Integer, TestCaseAssert> assertTestCases = Maps.newHashMap();
    private Collection<Consumer<Suite>> assertChain = Lists.newArrayList();
    private Consumer<Suite> assertDataSource;

    public SuiteAssert(SuiteAssert.Builder aBuilder) {
        this.assertFileAttribute = aBuilder.assertFileAttribute;
        this.assertScenario = aBuilder.assertScenario;
        this.assertScenarioChain = aBuilder.assertScenarioChain;
        this.assertTestCases.putAll(aBuilder.assertTestCases);
        this.assertChain.addAll(aBuilder.assertChain);
        this.assertDataSource = aBuilder.assertDataSource;
    }

    public static SuiteAssert.Builder of() {
        return new SuiteAssert.Builder();
    }

    public static Consumer<Suite> assertEqualsFileAttribute(File testSource) {
        return (Suite actual) -> {
            assertEquals(testSource.getName(), actual.name());
            assertEquals(testSource.getAbsolutePath(), actual.path());
            assertEquals(testSource.getParentFile().getAbsoluteFile(), actual.relativePath());
        };
    }

    public static void assertEqualsNoRelationFile(Suite result) {
        assertEquals(Suite.DEFAULT_NAME, result.name());
        assertEquals("", result.path());
    }

    public static void assertEqualsNoScript(Suite actual) {
        assertEquals(0, actual.scriptSize());
    }

    public static Consumer<Suite> assertEqualsTestCaseCount(int count) {
        return (Suite actual) -> assertEquals(count, actual.scriptSize());
    }

    public static void assertEqualsNoScriptChain(Suite actual) {
        assertEquals(0, actual.getScenario().chainSize());
    }

    public static Consumer<Suite> assertEqualsChainCount(int count) {
        return (Suite actual) -> assertEquals(count, actual.getScenario().chainSize());
    }

    public static void assertEqualsNoDataSource(Suite actual) {
        assertNull(actual.getTestDataSet().getDataSource());
        assertEquals(0, actual.getTestDataSet().getDataSourceConfig().size());
    }

    public static Consumer<Suite> assertEqualsDataSet(TestDataSet dataSource) {
        return (Suite actual) -> assertEquals(dataSource, actual.getTestDataSet());
    }

    public static Consumer<Suite> assertEqualsChain(int aChainFrom, int aChainTo) {
        return (Suite actual) -> assertEquals(actual.get(aChainTo), actual.getScenario().getChainTo(actual.get(aChainFrom)));
    }

    public void run(Suite target) {
        this.assertFileAttribute.accept(target);
        this.assertScenario.accept(target);
        this.assertScenarioChain.accept(target);
        this.assertTestCases.entrySet()
                .forEach(entry -> entry.getValue().run(target.get(entry.getKey())));
        this.assertChain.forEach(it -> it.accept(target));
        this.assertDataSource.accept(target);
    }

    public static class Builder {
        private Consumer<Suite> assertFileAttribute;
        private Consumer<Suite> assertScenario;
        private Consumer<Suite> assertScenarioChain;
        private Map<Integer, TestCaseAssert> assertTestCases = Maps.newHashMap();
        private Collection<Consumer<Suite>> assertChain = Lists.newArrayList();
        private Consumer<Suite> assertDataSource;

        public SuiteAssert.Builder assertFileAttribute(Consumer<Suite> assertion) {
            this.assertFileAttribute = assertion;
            return this;
        }

        public SuiteAssert.Builder assertTestCaseCount(Consumer<Suite> assertion) {
            this.assertScenario = assertion;
            return this;
        }

        public SuiteAssert.Builder assertChainCount(Consumer<Suite> assertion) {
            this.assertScenarioChain = assertion;
            return this;
        }

        public SuiteAssert.Builder assertDataSource(Consumer<Suite> assertion) {
            this.assertDataSource = assertion;
            return this;
        }

        public Builder assertTestCase(int i, TestCaseAssert testCaseAssert) {
            this.assertTestCases.put(i, testCaseAssert);
            return this;
        }

        public Builder assertChain(Consumer<Suite> suiteConsumer) {
            this.assertChain.add(suiteConsumer);
            return this;
        }

        public SuiteAssert create() {
            return new SuiteAssert(this);
        }

    }

}
