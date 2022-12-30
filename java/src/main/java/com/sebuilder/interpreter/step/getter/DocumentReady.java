package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.TestRun;

public class DocumentReady extends AbstractGetter {

    @Override
    public String get(final TestRun ctx) {
        return String.valueOf(ctx.executeScript("return document.readyState").equals("complete"));
    }

}
