package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

public class Loop implements FlowStep {
    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    public boolean run(TestRun ctx) {
        ctx.processTestSuccess();
        int actions = getSubSteps(ctx);
        int count = Integer.valueOf(ctx.string("count"));
        for (int i = 0; i < count; i++) {
            ctx.putVars("_index", String.valueOf(i + 1));
            if (!this.runSubStep(ctx, actions)) {
                ctx.processTestFailure();
                return false;
            }
            if (i + 1 < count) {
                ctx.processTestSuccess();
                ctx.backStepIndex(actions);
            }
        }
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("count")) {
            o.put("count", "");
        }
        return FlowStep.super.addDefaultParam(o);
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
