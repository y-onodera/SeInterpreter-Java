package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import org.json.JSONException;

public abstract class AbstractStepConverter implements StepConverter {
    @Override
    public Step toStep(SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        StepBuilder builder = getBuilder(converter, command);
        this.configure(builder, converter, command);
        return builder.build();
    }

    protected StepBuilder getBuilder(SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        return new StepBuilder(this.stepType());
    }

    protected abstract StepType stepType();

    protected abstract void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException;

    protected AbstractStepConverter addParam(StepBuilder builder, String key, String value) {
        builder.getStringParams().put(key, value);
        return this;
    }
}
