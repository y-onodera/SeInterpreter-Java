package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.getter.Text;
import com.sebuilder.interpreter.step.getter.Variable;
import org.json.JSONException;

public class AssertVariableConverter extends AbstractStepConverter {
    @Override
    protected StepType stepType() {
        return new Variable().toAssert();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        this.addParam(builder, "variable", command.target())
                .addParam(builder, "value", command.value());
    }
}
