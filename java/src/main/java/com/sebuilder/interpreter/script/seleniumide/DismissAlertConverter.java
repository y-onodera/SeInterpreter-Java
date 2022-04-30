package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.DismissAlert;
import org.json.JSONException;

public class DismissAlertConverter implements StepConverter {

    @Override
    public Step toStep(SeleniumIDEConverter converter, SeleniumIDECommand targetCommand) throws JSONException {
        return new StepBuilder(new DismissAlert()).build();
    }
}
