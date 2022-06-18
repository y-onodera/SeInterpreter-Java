package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

public class Loop extends AbstractStepType implements FlowStep {

    @Override
    public boolean run(TestRun ctx) {
        Step thisStep = ctx.currentStep();
        boolean success = true;
        ctx.processTestSuccess(false);
        int actions = getSubSteps(ctx);
        int count = Integer.parseInt(ctx.string("count"));
        for (int i = 0; i < count; i++) {
            ctx.putVars("_index", String.valueOf(i + 1));
            success = this.runSubStep(ctx, actions) && success;
            if (i + 1 < count) {
                ctx.backStepIndex(actions);
            }
        }
        ctx.removeVars("_index");
        ctx.getListener().startTest("End " + ctx.bindRuntimeVariables(thisStep.toPrettyString()));
        return success;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("count")) {
            o.put("count", "");
        }
        return FlowStep.super.addDefaultParam(o);
    }

}