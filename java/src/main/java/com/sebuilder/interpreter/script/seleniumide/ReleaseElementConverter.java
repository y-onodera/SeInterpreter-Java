package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.ReleaseElement;

public class ReleaseElementConverter extends AbstractLocatorStepConverter {

    @Override
    protected StepType stepType() {
        return new ReleaseElement();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        // no implements
    }
}
