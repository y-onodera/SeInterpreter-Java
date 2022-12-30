package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.getter.Variable;

public class AssertVariableConverter extends AbstractStepConverter {
    @Override
    protected StepType stepType() {
        return new Variable().toAssert();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        this.addParam(builder, "variable", command.target())
                .addParam(builder, "value", command.value());
    }
}
