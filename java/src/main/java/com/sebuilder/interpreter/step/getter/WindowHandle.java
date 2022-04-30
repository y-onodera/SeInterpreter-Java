package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.TestRun;

public class WindowHandle extends AbstractGetter {

    @Override
    public String get(TestRun ctx) {
        return ctx.driver().getWindowHandle();
    }
}
