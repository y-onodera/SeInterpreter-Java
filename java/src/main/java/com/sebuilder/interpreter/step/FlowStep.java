package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public interface FlowStep extends StepType {

    default int getSubSteps(TestRun ctx) {
        return Integer.parseInt(ctx.string("subStep"));
    }

    default boolean runSubStep(TestRun ctx, int actions) {
        boolean success = true;
        int exec = 0;
        while (exec < actions) {
            Step.Result result = ctx.runTest();
            exec = exec + result.execSteps();
            success = result.success() && success;
        }
        return success;
    }

    default void skipSubStep(TestRun ctx, int actions) {
        for (int i = 0; i < actions; i++) {
            ctx.skipTest();
        }
    }

    @Override
    default boolean isAcceptEndAdvice() {
        return false;
    }

    @Override
    default int getExecSteps(TestRun ctx) {
        return this.getSubSteps(ctx);
    }

    @Override
    default StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("subStep")) {
            o.put("subStep", "");
        }
        return o;
    }
}
