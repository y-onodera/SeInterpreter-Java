package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.FlowStep;

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
        int actions = getSubSteps(ctx);
        if (this.next(ctx)) {
            ctx.processTestSuccess();
            if (!this.runSubStep(ctx, actions)) {
                ctx.processTestFailure();
                return false;
            }
        } else {
            ctx.forwardStepIndex(actions);
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }

}
