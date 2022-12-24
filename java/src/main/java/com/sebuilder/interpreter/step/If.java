package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

import java.util.Objects;

public class If extends AbstractStepType implements FlowStep, GetterUseStep {

    private final Getter getter;

    public If(Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return this.getter;
    }

    @Override
    public boolean run(TestRun ctx) {
        Step thisStep = ctx.currentStep();
        boolean success = true;
        int actions = this.getSubSteps(ctx);
        if (this.test(ctx)) {
            ctx.processTestSuccess(false);
            success = this.runSubStep(ctx, actions);
        } else {
            ctx.processTestSuccess(false);
            this.skipSubStep(ctx, actions);
        }
        ctx.getListener().startTest("End " + ctx.bindRuntimeVariables(thisStep.toPrettyString()));
        return success;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        StepBuilder result = FlowStep.super.addDefaultParam(o);
        return GetterUseStep.super.addDefaultParam(result);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        If anIf = (If) o;
        return Objects.equals(this.getGetter(), anIf.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getGetter());
    }
}
