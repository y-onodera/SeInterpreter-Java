package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.AnswerAlert;

public class AnswerAlertConverter extends AbstractStepConverter {

    @Override
    protected StepType stepType() {
        return new AnswerAlert();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        this.addParam(builder, "text", command.target());
    }
}
