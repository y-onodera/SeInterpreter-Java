package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SetElementNotSelected;
import org.json.JSONException;

public class SetElementNotSelectedConverter extends AbstractLocatorStepConverter {

    @Override
    protected StepType stepType() {
        return new SetElementNotSelected();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        // no implements
    }
}
