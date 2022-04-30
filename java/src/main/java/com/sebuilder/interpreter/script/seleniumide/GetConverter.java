package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.Get;
import org.json.JSONException;

import java.util.Optional;

public class GetConverter extends AbstractStepConverter {
    @Override
    protected StepType stepType() {
        return new Get();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        if (Optional.ofNullable(command.target()).orElse("").startsWith("http")) {
            this.addParam(builder, "url", command.target());
        } else {
            this.addParam(builder, "url", converter.getUrl() + "/" + command.target()
                    .replaceAll("(?<!:)/{2,9}", "/"));
        }
    }
}
