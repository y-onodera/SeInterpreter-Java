
package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public interface FlowStep extends StepType {

    default boolean runSubStep(TestRun ctx, int actions) {
        boolean success = true;
        for (int exec = 0; exec < actions; exec++) {
            success = success && next(ctx);
            if (exec != actions - 1) {
                if (success) {
                    ctx.processTestSuccess();
                }
            }
        }
        return success;
    }

    default boolean next(TestRun ctx) {
        return ctx.runTest();
    }

    default int getSubSteps(TestRun ctx) {
        return Integer.valueOf(ctx.string("subStep"));
    }

    @Override
    default StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("subStep")) {
            o.put("subStep", "");
        }
        return o;
    }

}
