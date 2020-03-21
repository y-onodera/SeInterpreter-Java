package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.TestRun;

public class WindowWidth extends AbstractGetter {
    @Override
    public String get(TestRun ctx) {
        return String.valueOf(ctx.getWindowWidth());
    }

    @Override
    public String cmpParamName() {
        return "length";
    }
}
