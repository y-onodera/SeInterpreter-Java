package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.json.JSONException;

public class SetElementTextConverter extends AbstractLocatorStepConverter {
    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        this.addParam(builder, "text", command.value());
    }

    @Override
    protected StepType stepType() {
        return new SetElementText();
    }
}
