package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;

public class WindowMaximize extends AbstractStepType {
    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test finish.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    public boolean run(TestRun ctx) {
        ctx.driver().manage().window().maximize();
        return true;
    }

}
