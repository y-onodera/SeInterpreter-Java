package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.MouseOverElement;
import org.json.JSONException;

public class MouseOverElementConverter extends AbstractLocatorStepConverter {

    @Override
    protected StepType stepType() {
        return new MouseOverElement();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {

    }
}
