package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;

public class CloseWindow extends AbstractStepType {

    @Override
    public boolean run(final TestRun ctx) {
        ctx.driver().close();
        return true;
    }

}