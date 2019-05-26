package com.sebuilder.interpreter;

public interface TestRunner {
    void execute(Suite suite, TestRunListener seInterpreterTestListener);

    void execute(TestCase testCase, TestRunListener seInterpreterTestListener);
}
