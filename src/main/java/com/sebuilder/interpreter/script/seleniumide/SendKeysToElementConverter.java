package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SendKeysToElement;

public class SendKeysToElementConverter extends SetElementTextConverter {

    @Override
    protected StepType stepType() {
        return new SendKeysToElement();
    }
}
