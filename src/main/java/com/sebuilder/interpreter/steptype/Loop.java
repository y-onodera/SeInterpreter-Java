package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public class Loop implements StepType {
    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    public boolean run(TestRun ctx) {
        boolean success = true;
        int actions = Integer.valueOf(ctx.string("subStep"));
        int count = Integer.valueOf(ctx.string("count"));
        for (int i = 0; i < count; i++) {
            ctx.vars().put("_index", String.valueOf(i + 1));
            for (int exec = 0; exec < actions; exec++) {
                success = next(ctx) && success;
            }
            if (!success) {
                return false;
            }
            if (i + 1 < count) {
                ctx.backStepIndex(actions);
            }
        }
        return success;
    }

    private boolean next(TestRun ctx) {
        ctx.toNextStepIndex();
        return ctx.currentStep().type.run(ctx);
    }
}
