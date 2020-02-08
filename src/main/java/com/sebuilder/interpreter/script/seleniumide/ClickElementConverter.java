package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.ClickElement;
import org.json.JSONException;

public class ClickElementConverter extends AbstractLocatorStepConverter {
    @Override
    protected StepType stepType() {
        return new ClickElement();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        // nothing
    }
}
