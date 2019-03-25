package com.sebuilder.interpreter;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Scenario implements Iterable<TestCase> {

    private final ArrayList<TestCase> testCases;
    private final Map<TestCase, TestCase> chains;
    private Aspect aspect;

    public Scenario() {
        this(Lists.newArrayList(), Maps.newHashMap(), new Aspect());
    }

    public Scenario(TestCase testCase) {
        this(Lists.newArrayList(testCase), Maps.newHashMap(), new Aspect());
    }

    public Scenario(ArrayList<TestCase> aTestCases) {
        this(Lists.newArrayList(aTestCases), Maps.newHashMap(), new Aspect());
    }

    private Scenario(ArrayList<TestCase> newScripts, Map<TestCase, TestCase> newMap, Aspect newAspect) {
        this.chains = newMap;
        this.testCases = newScripts;
        this.aspect = newAspect;
    }

    @Override
    public Iterator<TestCase> iterator() {
        return this.testCaseIterator();
    }

    public Aspect aspect() {
        return this.aspect;
    }

    public Iterator<TestCase> testCaseIterator() {
        return this.testCases.iterator();
    }

    public int testCaseSize() {
        return this.testCases.size();
    }

    public int chainSize() {
        return this.chains.size();
    }

    public boolean hasChain(TestCase s) {
        return this.chains.containsKey(s);
    }

    public boolean isChainTarget(TestCase testCase) {
        return this.chains.containsValue(testCase);
    }

    public TestCase getChainTo(TestCase testCaseFrom) {
        return this.chains.get(testCaseFrom);
    }

    public TestCase get(String scriptName) {
        return this.testCases.stream()
                .filter(it -> it.name().equals(scriptName))
                .findFirst()
                .orElse(null);
    }

    public TestCase get(int index) {
        return this.testCases.get(index);
    }

    public int indexOf(TestCase aTestCase) {
        return this.testCases.indexOf(aTestCase);
    }

    public Scenario appendNewChain(TestCase chainFrom, TestCase to) {
        if (!this.testCases.contains(chainFrom) && !this.chains.containsValue(chainFrom)) {
            throw new IllegalArgumentException("testCases and chains not include" + chainFrom);
        }
        Map<TestCase, TestCase> newChain = Maps.newHashMap(this.chains);
        newChain.put(chainFrom, to);
        return new Scenario(this.testCases, newChain, this.aspect);
    }

    public Scenario append(Iterable<TestCase> aScripts) {
        ArrayList<TestCase> newList = Lists.newArrayList(this.testCases);
        Iterables.addAll(newList, aScripts);
        return new Scenario(newList, this.chains, this.aspect);
    }

    public Scenario append(TestCase testCase) {
        ArrayList<TestCase> newList = Lists.newArrayList(this.testCases);
        newList.add(testCase);
        return new Scenario(newList, this.chains, this.aspect);
    }

    public Scenario append(int aIndex, TestCase testCase) {
        ArrayList<TestCase> newList = Lists.newArrayList(this.testCases);
        newList.add(aIndex, testCase);
        return new Scenario(newList, this.chains, this.aspect);
    }

    public Scenario minus(TestCase aTestCase) {
        ArrayList<TestCase> newList = Lists.newArrayList(this.testCases);
        Map<TestCase, TestCase> newMap = Maps.newHashMap(this.chains);
        newList.remove(aTestCase);
        newMap.remove(aTestCase);
        this.chains.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(aTestCase))
                .findFirst()
                .ifPresent(entry -> newMap.remove(entry.getKey()));
        return new Scenario(newList, newMap, this.aspect);
    }

    public Scenario map(Function<TestCase, TestCase> converter) {
        ArrayList<TestCase> newTestCases = Lists.newArrayList();
        Map<TestCase, TestCase> newChains = this.chains;
        Map<String, Integer> duplicate = Maps.newHashMap();
        for (TestCase testCase : this.testCases) {
            TestCase copy = converter.apply(testCase);
            final String scriptName = copy.name();
            if (duplicate.containsKey(copy.path())) {
                Optional<String> entries = newTestCases
                        .stream()
                        .map(it -> it.name())
                        .filter(it -> scriptName.startsWith(it))
                        .findFirst();
                if (entries.isPresent()) {
                    int nextCount = duplicate.get(copy.path()) + 1;
                    duplicate.put(copy.path(), nextCount);
                    copy = copy.rename(entries.get() + String.format("(%d)", nextCount));
                }
            } else {
                duplicate.put(copy.path(), 0);
            }
            newTestCases.add(copy);
            newChains = this.getReplaceMap(testCase, copy, newChains);
        }
        return new Scenario(newTestCases, newChains, this.aspect);
    }

    public Scenario replaceTest(String oldName, TestCase aTestCase) {
        ArrayList<TestCase> newTestCases = new ArrayList<>(this.testCases.size());
        Map<TestCase, TestCase> newMap = this.chains;
        for (TestCase testCase : this.testCases) {
            TestCase newTestCase = testCase;
            if (testCase.name().equals(oldName)) {
                newTestCase = aTestCase;
                newMap = this.getReplaceMap(aTestCase, newTestCase);
            }
            newTestCases.add(newTestCase);
        }
        return new Scenario(newTestCases, newMap, this.aspect);
    }

    public Scenario addAspect(Aspect aspect) {
        return new Scenario(this.testCases, this.chains, aspect);
    }

    public Scenario lazyLoad(TestData aSource) {
        ArrayList<TestCase> newTestCases = new ArrayList<>(this.testCases.size());
        Map<TestCase, TestCase> newMap = this.chains;
        for (TestCase testCase : this.testCases) {
            TestCase loaded = testCase.lazyLoad(aSource);
            newTestCases.add(loaded);
            if (testCase != loaded) {
                newMap = this.getReplaceMap(testCase, loaded, newMap);
            }
        }
        return new Scenario(newTestCases, newMap, this.aspect);
    }

    public Stream<TestRunBuilder> getTestRuns(TestData param, Function<TestRunBuilder, TestRunBuilder> aFunction) {
        Scenario materialize = this.lazyLoad(param);
        return materialize
                .testRunStreams()
                .filter(script -> !script.skipRunning(param))
                .map(script -> new TestRunBuilder(script, materialize)
                        .setShareInput(param)
                ).map(aFunction);
    }

    private Map<TestCase, TestCase> getReplaceMap(TestCase oldTestCase, TestCase newTestCase) {
        return getReplaceMap(oldTestCase, newTestCase, this.chains);
    }

    private Map<TestCase, TestCase> getReplaceMap(TestCase oldTestCase, TestCase newTestCase, Map<TestCase, TestCase> oldChain) {
        Map<TestCase, TestCase> newChain = Maps.newHashMap(oldChain);
        if (newChain.containsKey(oldTestCase) || newChain.containsValue(oldTestCase)) {
            for (Map.Entry<TestCase, TestCase> entry : oldChain.entrySet()) {
                if (entry.getKey() == oldTestCase) {
                    newChain.remove(oldTestCase);
                    newChain.put(newTestCase, entry.getValue());
                } else if (entry.getValue() == oldTestCase) {
                    newChain.put(entry.getKey(), newTestCase);
                }
            }
        }
        return newChain;
    }

    private Stream<TestCase> testRunStreams() {
        return this.testCases.stream()
                .filter(script -> !this.isChainTarget(script));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Scenario scenario = (Scenario) o;
        return com.google.common.base.Objects.equal(testCases, scenario.testCases) &&
                com.google.common.base.Objects.equal(chains, scenario.chains) &&
                com.google.common.base.Objects.equal(aspect, scenario.aspect);
    }

    @Override
    public int hashCode() {
        return com.google.common.base.Objects.hashCode(testCases, chains, aspect);
    }
}