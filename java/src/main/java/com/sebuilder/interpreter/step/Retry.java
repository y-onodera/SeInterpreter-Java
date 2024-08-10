package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

import java.util.Objects;

public class Retry extends AbstractStepType implements FlowStep, GetterUseStep {

    private final Getter getter;

    public Retry(final Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return this.getter;
    }

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    public boolean run(final TestRun ctx) {
        final Step thisStep = ctx.currentStep();
        boolean success = true;
        final int actions = this.getSubSteps(ctx);
        while (!this.test(ctx) && !ctx.isStopped()) {
            ctx.processTestSuccess(false);
            success = this.runSubStep(ctx, actions) && success;
            ctx.backStepIndex(actions);
            ctx.startTest();
        }
        ctx.processTestSuccess(false);
        this.skipSubStep(ctx, actions);
        ctx.getListener().startTest("End " + ctx.bindRuntimeVariables(thisStep.toPrettyString()));
        return success;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        final StepBuilder result = FlowStep.super.addDefaultParam(o);
        return GetterUseStep.super.addDefaultParam(result);
    }

    @Override
    public boolean equals(final Object o) {
        if (!super.equals(o)) {
            return false;
        }
        final Retry retry = (Retry) o;
        return Objects.equals(this.getGetter(), retry.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getGetter());
    }
}
