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
        boolean result = this.getGetter().cmpParamName() == null
                ? Boolean.parseBoolean(got)
                : ctx.string(this.getGetter().cmpParamName()).equals(got);
        return result != ctx.currentStep().isNegated();
    }

    @Override
    default StepBuilder addDefaultParam(StepBuilder o) {
        return this.getGetter().addDefaultParam(o);
    }

}
