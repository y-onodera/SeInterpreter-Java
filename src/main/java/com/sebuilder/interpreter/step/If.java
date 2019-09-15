package com.sebuilder.interpreter.step;

import com.google.common.base.Objects;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

public class If extends AbstractStepType implements FlowStep, GetterUseStep {

    private final Getter getter;

    public If(Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return getter;
    }

    @Override
    public boolean run(TestRun ctx) {
        int actions = getSubSteps(ctx);
        if (this.test(ctx)) {
            ctx.processTestSuccess();
            if (!this.runSubStep(ctx, actions)) {
                ctx.processTestFailure();
                return false;
            }
        } else {
            ctx.processTestSuccess();
            this.skipSubStep(ctx, actions);
        }
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
        if (!super.equals(o)) return false;
        If anIf = (If) o;
        return Objects.equal(getGetter(), anIf.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getGetter());
    }
}
