package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SetElementText;

public class SetElementTextConverter extends AbstractLocatorStepConverter {
    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        this.addParam(builder, "text", command.value());
    }

    @Override
    protected StepType stepType() {
        return new SetElementText();
    }
}
