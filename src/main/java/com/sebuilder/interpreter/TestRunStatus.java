package com.sebuilder.interpreter;

public class TestRunStatus {
    private final int stepIndex;
    private final boolean chainRun;
    private final boolean finished;
    private final boolean stopped;

    private TestRunStatus(int stepIndex, boolean chainRun, boolean finished, boolean stop) {
        this.stepIndex = stepIndex;
        this.chainRun = chainRun;
        this.finished = finished;
        this.stopped = stop;
    }

    public static TestRunStatus of(TestCase testCase) {
        return new TestRunStatus(-1, testCase.hasChain(), false, false);
    }

    public TestRunStatus start() {
        return new TestRunStatus(this.stepIndex, this.chainRun, false, false);
    }

    public TestRunStatus forwardStepIndex(int count) {
        return new TestRunStatus(this.stepIndex + count, this.chainRun, this.finished, this.stopped);
    }

    public TestRunStatus chainCalled() {
        return new TestRunStatus(this.stepIndex, false, this.finished, this.stopped);
    }

    public TestRunStatus finish() {
        return new TestRunStatus(this.stepIndex, this.chainRun, true, this.stopped);
    }

    public TestRunStatus stop() {
        return new TestRunStatus(this.stepIndex, this.chainRun, this.finished, true);
    }

    public boolean isStopped() {
        return this.stopped;
    }

    public boolean isNeedChain() {
        return !this.isStopped() && this.chainRun;
    }

    public boolean isNeedRunning(int aIndex) {
        return !this.isStopped() && this.stepIndex < aIndex;
    }

    public int stepIndex() {
        return this.stepIndex;
    }

}