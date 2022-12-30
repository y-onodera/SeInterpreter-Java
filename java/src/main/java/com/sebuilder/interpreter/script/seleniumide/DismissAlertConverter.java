package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.DismissAlert;

public class DismissAlertConverter implements StepConverter {

    @Override
    public Step toStep(final SeleniumIDEConverter converter, final SeleniumIDECommand targetCommand) {
        return new StepBuilder(new DismissAlert()).build();
    }
}
