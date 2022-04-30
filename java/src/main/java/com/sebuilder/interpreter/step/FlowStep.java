package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public interface FlowStep extends StepType {

    default int getSubSteps(TestRun ctx) {
        return Integer.parseInt(ctx.string("subStep"));
    }

    default boolean runSubStep(TestRun ctx, int actions) {
        boolean success = true;
        for (int exec = 0; exec < actions; exec++) {
            ctx.toNextStepIndex();
            ctx.startTest();
            int subStep = 0;
            if (ctx.currentStep().getType() instanceof FlowStep) {
                subStep = this.getSubSteps(ctx);
            }
            success = success && ctx.currentStep().run(ctx);
            exec = exec + subStep;
            if (exec != actions - 1) {
                if (success) {
                    ctx.processTestSuccess();
                } else {
                    ctx.processTestFailure();
                }
            }
        }
        return success;
    }

    default void skipSubStep(TestRun ctx, int actions) {
        for (int i = 0; i < actions; i++) {
            ctx.toNextStepIndex();
            ctx.getListener().startTest(
                    ctx.bindRuntimeVariables(
                            ctx.currentStep()
                                    .builder()
                                    .put("skip", "true")
                                    .build()
                                    .toPrettyString()
                    )
            );
            if (i != actions - 1) {
                ctx.processTestSuccess();
            }
        }
    }

    @Override
    default StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("subStep")) {
            o.put("subStep", "");
        }
        return o;
    }

}
