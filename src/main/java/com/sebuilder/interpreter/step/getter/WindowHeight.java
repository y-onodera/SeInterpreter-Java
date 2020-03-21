package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.TestRun;

public class WindowHeight extends AbstractGetter {
    @Override
    public String get(TestRun ctx) {
        return String.valueOf(ctx.getWindowHeight());
    }

    @Override
    public String cmpParamName() {
        return "length";
    }
}
