package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

import java.util.Objects;

public class Print extends AbstractStepType implements GetterUseStep {
    public final Getter getter;

    public Print(final Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return this.getter;
    }

    @Override
    public boolean run(final TestRun ctx) {
        String value = this.getter.get(ctx);
        if (ctx.currentStep().negated()) {
            value = String.valueOf(!Boolean.parseBoolean(value));
        }
        ctx.log().info(value);
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        return GetterUseStep.super.addDefaultParam(o);
    }

    @Override
    public boolean equals(final Object o) {
        if (!super.equals(o)) {
            return false;
        }
        final Print print = (Print) o;
        return Objects.equals(this.getGetter(), print.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), this.getGetter());
    }
}
