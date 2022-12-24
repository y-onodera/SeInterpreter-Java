package com.sebuilder.interpreter;

import com.google.common.base.Strings;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public record TestCaseChains(ArrayList<TestCase> testCases, boolean takeOverLastRun) implements Iterable<TestCase> {

    public TestCaseChains() {
        this(new ArrayList<>(), false);
    }

    @Override
    @Nonnull
    public Iterator<TestCase> iterator() {
        return this.testCases.iterator();
    }

    public Stream<TestCase> flattenTestCases() {
        return this.testCases.stream()
                .flatMap(TestCase::flattenTestCases);
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
        return new TestCaseChains(new ArrayList<>(this.testCases), isTakeOverLastRun);
    }

    public TestCaseChains append(TestCase testCase) {
        return this.append(this.size(), testCase);
    }

    public TestCaseChains append(int aIndex, TestCase testCase) {
        ArrayList<TestCase> newList = new ArrayList<>(this.testCases);
        long countDuplicate = newList.stream().filter(it -> !Strings.isNullOrEmpty(it.scriptFile().path())
                && !Strings.isNullOrEmpty(testCase.scriptFile().path())
                && it.scriptFile().path().equals(testCase.scriptFile().path())).count();
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
                .map(it -> it.map(builder -> builder.setChains(it.chains().remove(aTestCase))))
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
        }, it -> !(it.equals(aTestCase) && aTestCase.scriptFile().type() == ScriptFile.Type.SUITE));
    }

    public TestCaseChains map(Function<TestCase, TestCase> converter, Predicate<TestCase> isChainConvert) {
        ArrayList<TestCase> newTestCases = new ArrayList<>();
        Map<Pair<String, String>, Integer> duplicate = new HashMap<>();
        for (TestCase testCase : this.testCases) {
            TestCase copy = converter
                    .apply(testCase)
                    .changeWhenConditionMatch(isChainConvert, matches -> matches.map(it -> it.setChains(matches.chains().map(converter, isChainConvert))));
            String scriptName = copy.name();
            Pair<String, String> key = Pair.of(copy.name(), copy.path());
            if (duplicate.containsKey(key) && !Strings.isNullOrEmpty(key.getValue())) {
                Optional<String> entries = newTestCases
                        .stream()
                        .map(TestCase::name)
                        .filter(scriptName::startsWith)
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

    TestCase selectRecursive(String scriptName, TestCaseChains chain) {
        for (TestCase target : chain) {
            if (target.name().equals(scriptName)) {
                return target;
            }
            TestCase recursiveSearch = this.selectRecursive(scriptName, target.chains());
            if (recursiveSearch != null) {
                return recursiveSearch;
            }
        }
        return null;
    }

    TestCase renameDuplicateCase(TestCase target, long nextCount) {
        return target.map(builder -> builder.setName(target.fileName() + String.format("(%d)", nextCount)));
    }

}
