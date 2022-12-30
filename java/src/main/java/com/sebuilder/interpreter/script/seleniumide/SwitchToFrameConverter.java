package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SwitchToDefaultContent;
import com.sebuilder.interpreter.step.type.SwitchToFrameByIndex;

public class SwitchToFrameConverter extends AbstractStepConverter {

    @Override
    protected StepBuilder getBuilder(final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        if (command.target().startsWith("relative=")) {
            return new StepBuilder(new SwitchToDefaultContent());
        }
        return super.getBuilder(converter, command);
    }

    @Override
    protected StepType stepType() {
        return new SwitchToFrameByIndex();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        if (builder.getStepType() instanceof SwitchToFrameByIndex) {
            this.addParam(builder, "index", command.target().replace("index=", ""));
        }
    }
}
