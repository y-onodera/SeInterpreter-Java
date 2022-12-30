package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.ClickElement;

public class ClickElementConverter extends AbstractLocatorStepConverter {
    @Override
    protected StepType stepType() {
        return new ClickElement();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        // nothing
    }
}
