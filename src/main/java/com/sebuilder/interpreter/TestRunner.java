package com.sebuilder.interpreter;

public interface TestRunner {
    void execute(Suite suite, SeInterpreterTestListener seInterpreterTestListener);

    void execute(TestCase testCase, SeInterpreterTestListener seInterpreterTestListener);
}
