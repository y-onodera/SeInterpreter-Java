package com.sebuilder.interpreter;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Function;

public class TestCaseBuilder extends AbstractTestRunnable.AbstractBuilder<TestCase, TestCaseBuilder> {
    private ArrayList<Step> steps;
    private boolean closeDriver;

    public TestCaseBuilder() {
        super(new ScriptFile(TestCase.DEFAULT_SCRIPT_NAME));
        this.steps = new ArrayList<>();
        this.closeDriver = true;
        this.setSkip("false");
    }

    public TestCaseBuilder(TestCase currentDisplay) {
        super(currentDisplay);
        this.steps = new ArrayList<>(currentDisplay.steps());
        this.closeDriver = currentDisplay.closeDriver();
    }

    public static TestCase lazyLoad(String beforeReplace, Function<TestData, TestRunnable> lazyLoad) {
        return new TestCaseBuilder()
                .setName(beforeReplace)
                .setLazyLoad(lazyLoad)
                .build();
    }

    @Override
    public TestCase build() {
        return new TestCase(this);
    }

    @Override
    public TestCaseBuilder associateWith(File target) {
        this.setScriptFile(ScriptFile.of(target, TestCase.DEFAULT_SCRIPT_NAME));
        return this;
    }

    @Override
    public TestCaseBuilder isShareState(boolean shareState) {
        if (shareState) {
            this.closeDriver = false;
        } else {
            this.closeDriver = true;
        }
        return super.isShareState(shareState);
    }

    @Override
    protected TestCaseBuilder self() {
        return this;
    }

    public ArrayList<Step> getSteps() {
        return this.steps;
    }

    public File getRelativePath() {
        return this.getScriptFile().relativePath();
    }

    public boolean isCloseDriver() {
        return this.closeDriver;
    }

    public TestCaseBuilder clearStep() {
        this.steps.clear();
        return this;
    }

    public TestCaseBuilder addSteps(ArrayList<Step> steps) {
        this.steps.addAll(steps);
        return this;
    }

    public TestCaseBuilder addStep(Step aStep) {
        this.steps.add(aStep);
        return this;
    }

}