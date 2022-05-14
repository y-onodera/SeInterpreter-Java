package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;

public interface GetterUseStep extends StepType {

    Getter getGetter();

    @Override
    default String getStepTypeName() {
        final String typeName = StepType.super.getStepTypeName();
        return typeName.substring(0, 1).toLowerCase() + typeName.substring(1) + this.getGetter().getClass().getSimpleName();
    }

    default boolean test(TestRun ctx) {
        String got = this.getGetter().get(ctx);
        boolean result;
        if (this.getGetter().cmpParamName() == null) {
            result = Boolean.parseBoolean(got);
        } else {
            ctx.getListener().info("actual:" + got);
            String expect = ctx.string(this.getGetter().cmpParamName());
            ctx.getListener().info("expect:" + expect);
            result = expect.equals(got);
        }
        return result != ctx.currentStep().negated();
    }

    @Override
    default StepBuilder addDefaultParam(StepBuilder o) {
        return this.getGetter().addDefaultParam(o);
    }

}
