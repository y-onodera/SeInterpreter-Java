package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.AcceptAlert;
import org.json.JSONException;

public class AcceptAlertConverter implements StepConverter {

    @Override
    public Step toStep(SeleniumIDEConverter converter, SeleniumIDECommand targetCommand) throws JSONException {
        return new StepBuilder(new AcceptAlert()).build();
    }

}
