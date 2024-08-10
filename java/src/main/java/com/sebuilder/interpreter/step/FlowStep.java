package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

import java.util.stream.IntStream;

public interface FlowStep extends StepType {

    default int getSubSteps(final TestRun ctx) {
        return Integer.parseInt(ctx.string("subStep"));
    }

    default boolean runSubStep(final TestRun ctx, final int actions) {
        boolean success = true;
        int exec = 0;
        while (exec < actions && !ctx.isStopped()) {
            final Step.Result result = ctx.runStep();
            exec = exec + result.execSteps();
            success = result.success() && success;
        }
        return success;
    }

    default void skipSubStep(final TestRun ctx, final int actions) {
        IntStream.range(0, actions).forEach(i -> ctx.skipTest());
    }

    @Override
    default boolean isAcceptEndAdvice() {
        return false;
    }

    @Override
    default int getExecSteps(final TestRun ctx) {
        return this.getSubSteps(ctx);
    }

    @Override
    default StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("subStep")) {
            o.put("subStep", "");
        }
        return o;
    }
}
