package com.sebuilder.interpreter.step;

import com.google.common.base.Objects;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

public class Print extends AbstractStepType implements GetterUseStep {
    public final Getter getter;

    public Print(Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return getter;
    }

    @Override
    public boolean run(TestRun ctx) {
        String value = getter.get(ctx);
        if (ctx.currentStep().isNegated()) {
            value = String.valueOf(!Boolean.valueOf(value));
        }
        ctx.log().info(value);
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        return GetterUseStep.super.addDefaultParam(o);
    }

    @Override
    public boolean equals(Object o) {
        if (!super.equals(o)) {
            return false;
        }
        Print print = (Print) o;
        return Objects.equal(getGetter(), print.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(super.hashCode(), getGetter());
    }
}
