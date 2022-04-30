package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;
import org.json.JSONException;

public interface StepConverter {

    Step toStep(SeleniumIDEConverter converter, SeleniumIDECommand targetCommand) throws JSONException;

}
