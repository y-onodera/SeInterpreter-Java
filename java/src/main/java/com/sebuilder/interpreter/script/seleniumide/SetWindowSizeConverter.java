package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SetWindowSize;
import org.json.JSONException;

public class SetWindowSizeConverter extends AbstractStepConverter {

    @Override
    protected StepType stepType() {
        return new SetWindowSize();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        String[] size = command.target().split("x", 2);
        if (size.length == 2) {
            this.addParam(builder, "width", size[0])
                    .addParam(builder, "height", size[1]);
        }
    }
}
