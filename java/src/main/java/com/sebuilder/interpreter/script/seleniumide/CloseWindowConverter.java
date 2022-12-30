package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.CloseWindow;

public class CloseWindowConverter implements StepConverter {

    @Override
    public Step toStep(final SeleniumIDEConverter converter, final SeleniumIDECommand targetCommand) {
        return new StepBuilder(new CloseWindow()).build();
    }
}
