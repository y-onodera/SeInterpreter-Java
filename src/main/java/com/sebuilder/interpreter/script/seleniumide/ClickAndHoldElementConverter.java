package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.ClickAndHoldElement;
import org.json.JSONException;

public class ClickAndHoldElementConverter extends AbstractLocatorStepConverter {

    @Override
    protected StepType stepType() {
        return new ClickAndHoldElement();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        // no implements
    }
}
