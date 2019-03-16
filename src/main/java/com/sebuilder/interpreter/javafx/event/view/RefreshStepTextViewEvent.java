package com.sebuilder.interpreter.javafx.event.view;

import com.sebuilder.interpreter.TestCase;

public class RefreshStepTextViewEvent {

    private final TestCase testCase;

    public RefreshStepTextViewEvent(TestCase testCase) {
        this.testCase = testCase;
    }

    public TestCase getTestCase() {
        return testCase;
    }
}
