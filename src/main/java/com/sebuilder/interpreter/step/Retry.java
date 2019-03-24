package com.sebuilder.interpreter.step;

import com.google.common.base.Objects;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

public class Retry implements FlowStep, GetterUseStep {

    private final Getter getter;

    public Retry(Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return getter;
    }

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
        while (!this.test(ctx)) {
            if (!this.runSubStep(ctx, actions)) {
                ctx.processTestFailure();
                return false;
            }
            ctx.processTestSuccess();
            ctx.backStepIndex(actions);
        }
        ctx.processTestSuccess();
        ctx.forwardStepIndex(actions);
        ctx.startTest();
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        StepBuilder result = FlowStep.super.addDefaultParam(o);
        return GetterUseStep.super.addDefaultParam(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Retry retry = (Retry) o;
        return Objects.equal(getGetter(), retry.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getGetter());
    }
}
