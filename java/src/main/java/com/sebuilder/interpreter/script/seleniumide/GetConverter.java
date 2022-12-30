package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.Get;

import java.util.Optional;

public class GetConverter extends AbstractStepConverter {
    @Override
    protected StepType stepType() {
        return new Get();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        if (Optional.ofNullable(command.target()).orElse("").startsWith("http")) {
            this.addParam(builder, "url", command.target());
        } else {
            this.addParam(builder, "url", converter.getUrl() + "/" + command.target()
                    .replaceAll("(?<!:)/{2,9}", "/"));
        }
    }
}
