package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SwitchToWindow;

public class SwitchToWindowConverter extends AbstractStepConverter {

    @Override
    protected StepType stepType() {
        return new SwitchToWindow();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        this.addParam(builder, "name", command.target().replace("handle=", ""));
    }
}
