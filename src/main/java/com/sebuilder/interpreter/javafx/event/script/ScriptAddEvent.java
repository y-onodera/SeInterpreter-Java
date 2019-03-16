package com.sebuilder.interpreter.javafx.event.script;

import com.sebuilder.interpreter.TestCase;

public class ScriptAddEvent {
    private final TestCase testCase;

    public ScriptAddEvent() {
        this(null);
    }

    public ScriptAddEvent(TestCase testCase) {
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
