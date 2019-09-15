package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

public class Loop extends AbstractStepType implements FlowStep {

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

}
