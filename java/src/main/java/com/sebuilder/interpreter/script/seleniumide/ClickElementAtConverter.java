package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.ClickElementAt;

public class ClickElementAtConverter extends AbstractLocatorStepConverter {

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        final String[] point = command.value().split(",");
        if (point.length == 2) {
            this.addParam(builder, "pointX", point[0])
                    .addParam(builder, "pointY", point[1]);
        }
    }

    @Override
    protected StepType stepType() {
        return new ClickElementAt();
    }
}
