package com.sebuilder.interpreter;

public interface TestCaseConverter {

    String toString(Suite target);

    String toString(TestCase target);

    String toString(Aspect target);
}
