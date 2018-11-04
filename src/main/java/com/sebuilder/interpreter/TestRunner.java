package com.sebuilder.interpreter;

public interface TestRunner {
    void execute(Suite suite, SeInterpreterTestListener seInterpreterTestListener);

    void execute(Script script, SeInterpreterTestListener seInterpreterTestListener);
}
