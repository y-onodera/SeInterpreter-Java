package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SetWindowSize;

public class SetWindowSizeConverter extends AbstractStepConverter {

    @Override
    protected StepType stepType() {
        return new SetWindowSize();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        final String[] size = command.target().split("x", 2);
        if (size.length == 2) {
            this.addParam(builder, "width", size[0])
                    .addParam(builder, "height", size[1]);
        }
    }
}
