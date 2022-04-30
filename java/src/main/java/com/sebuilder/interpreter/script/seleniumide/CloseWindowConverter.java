package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.CloseWindow;
import org.json.JSONException;

public class CloseWindowConverter implements StepConverter {

    @Override
    public Step toStep(SeleniumIDEConverter converter, SeleniumIDECommand targetCommand) throws JSONException {
        return new StepBuilder(new CloseWindow()).build();
    }
}
