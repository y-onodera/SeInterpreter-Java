package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.AnswerAlert;
import org.json.JSONException;

public class AnswerAlertConverter extends AbstractStepConverter {

    @Override
    protected StepType stepType() {
        return new AnswerAlert();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        this.addParam(builder, "text", command.target());
    }
}
