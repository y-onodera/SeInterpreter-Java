package com.sebuilder.interpreter.javafx.event.view;

import com.sebuilder.interpreter.TestCase;

public class RefreshStepTableViewEvent {

    private final TestCase testCase;

    public RefreshStepTableViewEvent(TestCase testCase) {
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
