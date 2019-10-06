package com.sebuilder.interpreter;

public interface TestRunner {

    boolean execute(TestRunBuilder testRunBuilder, TestData data, TestRunListener testRunListener);

}
