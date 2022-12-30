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

    default boolean test(final TestRun ctx) {
        return this.getGetter().test(ctx) != ctx.currentStep().negated();
    }

    @Override
    default StepBuilder addDefaultParam(final StepBuilder o) {
        return this.getGetter().addDefaultParam(o);
    }

}
