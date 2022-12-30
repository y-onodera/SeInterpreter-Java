package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Step;

public interface StepConverter {

    Step toStep(SeleniumIDEConverter converter, SeleniumIDECommand targetCommand);

}
