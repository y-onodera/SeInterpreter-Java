package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public class Scenario implements Iterable<TestCase> {

    private final ArrayList<? extends TestRunnable> testCases;
    private final Map<? extends TestRunnable, ? extends TestRunnable> chains;
    private Aspect aspect;

    public Scenario() {
        this(Lists.newArrayList(), Maps.newHashMap(), new Aspect());
    }

    public Scenario(TestRunnable testCase) {
        this(Lists.newArrayList(testCase), Maps.newHashMap(), new Aspect());
    }

    private Scenario(ArrayList<? extends TestRunnable> newScripts, Map<? extends TestRunnable, ? extends TestRunnable> newMap, Aspect newAspect) {
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
        return this.testCases.stream()
                .map(TestRunnable::head)
                .iterator();
    }

    public int testCaseSize() {
        return this.testCases.size();
    }

    public int chainSize() {
        return this.chains.size();
    }

    public boolean hasChain(TestRunnable s) {
        return this.chains.containsKey(s);
    }

    public boolean isChainTarget(TestRunnable testCase) {
        return this.chains.containsValue(testCase);
    }

    public TestCase getChainTo(TestRunnable testCaseFrom) {
        return this.chains.get(testCaseFrom).head();
    }

    public TestCase get(String scriptName) {
        return this.testCases.stream()
                .filter(it -> it.name().equals(scriptName))
                .map(TestRunnable::head)
                .findFirst()
                .orElse(null);
    }

    public TestCase get(int index) {
        return this.testCases.get(index).head();
    }

    public int indexOf(TestCase aTestCase) {
        return this.testCases.indexOf(aTestCase);
    }

    public Scenario appendNewChain(TestRunnable chainFrom, TestRunnable to) {
        if (!this.testCases.contains(chainFrom) && !this.chains.containsValue(chainFrom)) {
            throw new IllegalArgumentException("testCases and chains not include " + chainFrom);
        }
        Map<TestRunnable, TestRunnable> newChain = Maps.newHashMap(this.chains);
        newChain.put(chainFrom, to);
        return new Scenario(this.testCases, newChain, this.aspect);
    }

    public Scenario append(TestRunnable testCase) {
        return this.append(this.testCaseSize(), testCase);
    }

    public Scenario append(int aIndex, TestRunnable testCase) {
        ArrayList<TestRunnable> newList = Lists.newArrayList(this.testCases);
        int countDuplicate = (int) this.testCases.stream().filter(it -> it.equals(testCase)).count();
        if (countDuplicate == 0) {
            newList.add(aIndex, testCase);
        } else {
            newList.add(aIndex, this.renameDuplicateCase(testCase, countDuplicate));
        }
        return new Scenario(newList, this.chains, this.aspect);
    }

    public Scenario minus(TestCase aTestCase) {
        ArrayList<TestRunnable> newList = Lists.newArrayList(this.testCases);
        Map<TestRunnable, TestRunnable> newMap = Maps.newHashMap(this.chains);
        newList.remove(aTestCase);
        newMap.remove(aTestCase);
        this.chains.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(aTestCase))
                .findFirst()
                .ifPresent(entry -> newMap.remove(entry.getKey()));
        return new Scenario(newList, newMap, this.aspect);
    }

    public Scenario replaceTest(String oldName, TestRunnable aTestCase) {
        return this.map(testCase -> {
            if (testCase.name().equals(oldName)) {
                return aTestCase;
            }
            return testCase;
        });
    }

    public Scenario lazyLoad(TestData aSource) {
        return this.map(testCase -> testCase.lazyLoad(aSource).head());
    }

    public Scenario map(Function<TestRunnable, TestRunnable> converter) {
        ArrayList<TestRunnable> newTestCases = Lists.newArrayList();
        Map<TestRunnable, TestRunnable> newChains = Maps.newHashMap(this.chains);
        Map<String, Integer> duplicate = Maps.newHashMap();
        for (Iterable<TestRunnable> testCases : this.testCases) {
            for (TestRunnable testCase : testCases) {
                TestRunnable copy = converter.apply(testCase);
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
                        copy = this.renameDuplicateCase(copy, nextCount);
                    }
                } else {
                    duplicate.put(copy.path(), 0);
                }
                newTestCases.add(copy);
                newChains = this.getReplaceMap(testCase, copy, newChains);
            }
        }
        return new Scenario(newTestCases, newChains, this.aspect);
    }

    public Scenario addAspect(Aspect aspect) {
        return new Scenario(this.testCases, this.chains, this.aspect.builder().add(aspect).build());
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

    protected <T extends TestRunnable> T renameDuplicateCase(TestRunnable<T> target, int nextCount) {
        return target.rename(target.fileName() + String.format("(%d)", nextCount));
    }

    private Map<TestRunnable, TestRunnable> getReplaceMap(TestRunnable oldTestCase, TestRunnable newTestCase, Map<TestRunnable, TestRunnable> oldChain) {
        Map<TestRunnable, TestRunnable> newChain = Maps.newHashMap(oldChain);
        if (newChain.containsKey(oldTestCase) || newChain.containsValue(oldTestCase)) {
            for (Map.Entry<TestRunnable, TestRunnable> entry : oldChain.entrySet()) {
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
                .filter(script -> !this.isChainTarget(script))
                .map(TestRunnable::head);
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
