package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TestCaseChains implements Iterable<TestCase> {

    private final ArrayList<TestCase> testCases;

    private final boolean takeOverLastRun;

    public TestCaseChains() {
        this(Lists.newArrayList(), false);
    }

    public TestCaseChains(ArrayList<TestCase> testCases, boolean takeOverLastRun) {
        this.testCases = testCases;
        this.takeOverLastRun = takeOverLastRun;
    }

    @Override
    public Iterator<TestCase> iterator() {
        return this.testCases.iterator();
    }

    public boolean isTakeOverLastRun() {
        return this.takeOverLastRun;
    }

    public int size() {
        return this.testCases.size();
    }

    public TestCase get(String scriptName) {
        return this.selectRecursive(scriptName, this);
    }

    public TestCase get(int index) {
        return this.testCases.get(index);
    }

    public int indexOf(TestCase aTestCase) {
        return this.testCases.indexOf(aTestCase);
    }

    public TestCaseChains takeOverLastRun(boolean isTakeOverLastRun) {
        return new TestCaseChains(Lists.newArrayList(this.testCases), isTakeOverLastRun);
    }

    public TestCaseChains append(TestCase testCase) {
        return this.append(this.size(), testCase);
    }

    public TestCaseChains append(int aIndex, TestCase testCase) {
        ArrayList<TestCase> newList = Lists.newArrayList(this.testCases);
        long countDuplicate = newList.stream().filter(it -> it.equals(testCase)).count();
        if (countDuplicate == 0) {
            newList.add(aIndex, testCase);
        } else {
            newList.add(aIndex, this.renameDuplicateCase(testCase, countDuplicate));
        }
        return new TestCaseChains(newList, this.takeOverLastRun);
    }

    public TestCaseChains remove(TestCase aTestCase) {
        ArrayList<TestCase> newList = this.testCases
                .stream()
                .map(it -> it.map(builder -> builder.setChains(it.getChains().remove(aTestCase))))
                .filter(it -> !it.name().equals(aTestCase.name()))
                .collect(Collectors.toCollection(ArrayList::new));
        return new TestCaseChains(newList, this.takeOverLastRun);
    }

    public TestCaseChains replaceTest(TestCase oldTestCase, TestCase aTestCase) {
        return this.map(testCase -> {
            if (testCase.equals(oldTestCase)) {
                return aTestCase;
            }
            return testCase;
        }, it -> !(it.equals(aTestCase) && aTestCase.getScriptFile().type() == ScriptFile.Type.SUITE));
    }

    public TestCaseChains map(Function<TestCase, TestCase> converter, Predicate<TestCase> isChainConvert) {
        ArrayList<TestCase> newTestCases = Lists.newArrayList();
        Map<Pair<String, String>, Integer> duplicate = Maps.newHashMap();
        for (TestCase testCase : this.testCases) {
            TestCase copy = converter
                    .apply(testCase)
                    .changeWhenConditionMatch(isChainConvert, matches -> matches.map(it -> it.setChains(testCase.getChains().map(converter, isChainConvert))));
            final String scriptName = copy.name();
            Pair<String, String> key = Pair.of(copy.name(), copy.path());
            if (duplicate.containsKey(key)) {
                Optional<String> entries = newTestCases
                        .stream()
                        .map(it -> it.name())
                        .filter(it -> scriptName.startsWith(it))
                        .findFirst();
                if (entries.isPresent()) {
                    int nextCount = duplicate.get(key) + 1;
                    duplicate.put(key, nextCount);
                    copy = this.renameDuplicateCase(copy, nextCount);
                }
            } else {
                duplicate.put(key, 0);
            }
            newTestCases.add(copy);
        }
        return new TestCaseChains(newTestCases, this.takeOverLastRun);
    }

    protected TestCase selectRecursive(String scriptName, TestCaseChains chain) {
        for (TestCase target : chain) {
            if (target.name().equals(scriptName)) {
                return target;
            }
            TestCase recursiveSearch = this.selectRecursive(scriptName, target.getChains());
            if (recursiveSearch != null) {
                return recursiveSearch;
            }
        }
        return null;
    }

    protected TestCase renameDuplicateCase(TestCase target, long nextCount) {
        return target.map(builder -> builder.setName(target.fileName() + String.format("(%d)", nextCount)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TestCaseChains testCases1 = (TestCaseChains) o;
        return isTakeOverLastRun() == testCases1.isTakeOverLastRun() &&
                Objects.equal(testCases, testCases1.testCases);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(testCases, isTakeOverLastRun());
    }

}
