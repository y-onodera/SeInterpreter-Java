package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.TestRun;

public class CloseWindow extends AbstractStepType {

    @Override
    public boolean run(TestRun ctx) {
        ctx.driver().close();
        return true;
    }

}