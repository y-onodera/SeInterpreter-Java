package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SwitchToWindow;
import org.json.JSONException;

public class SwitchToWindowConverter extends AbstractStepConverter {

    @Override
    protected StepType stepType() {
        return new SwitchToWindow();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        this.addParam(builder, "name", command.target().replace("handle=", ""));
    }
}
