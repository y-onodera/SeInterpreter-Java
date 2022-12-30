package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;

public abstract class AbstractStepConverter implements StepConverter {
    @Override
    public Step toStep(final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        final StepBuilder builder = this.getBuilder(converter, command);
        this.configure(builder, converter, command);
        return builder.build();
    }

    protected StepBuilder getBuilder(final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        return new StepBuilder(this.stepType());
    }

    protected abstract StepType stepType();

    protected abstract void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command);

    protected AbstractStepConverter addParam(final StepBuilder builder, final String key, final String value) {
        builder.getStringParams().put(key, value);
        return this;
    }
}
