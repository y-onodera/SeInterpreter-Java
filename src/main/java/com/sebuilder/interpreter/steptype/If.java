package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.FlowStep;
import com.sebuilder.interpreter.TestRun;

public class If implements FlowStep {

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    public boolean run(TestRun ctx) {
        ctx.processTestSuccess();
        boolean success = true;
        int actions = getSubSteps(ctx);
        final boolean isTrue = this.next(ctx);
        ctx.processTestSuccess();
        if (isTrue) {
            success = this.runSubStep(ctx, success, actions);
            if (!success) {
                ctx.processTestFailure();
                return false;
            }
        } else {
            ctx.forwardStepIndex(actions);
        }
        return true;
    }

}
