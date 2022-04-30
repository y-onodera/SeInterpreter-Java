package com.sebuilder.interpreter;

public interface TestRunner {

    STATUS execute(TestRunBuilder testRunBuilder, InputData data, TestRunListener testRunListener);

    enum STATUS {
        SUCCESS, FAILED, STOPPED
    }
}
