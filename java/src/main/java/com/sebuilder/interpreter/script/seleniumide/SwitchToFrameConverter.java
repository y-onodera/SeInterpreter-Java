package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SwitchToDefaultContent;
import com.sebuilder.interpreter.step.type.SwitchToFrameByIndex;
import org.json.JSONException;

public class SwitchToFrameConverter extends AbstractStepConverter {

    @Override
    protected StepBuilder getBuilder(SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        if (command.target().startsWith("relative=")) {
            return new StepBuilder(new SwitchToDefaultContent());
        }
        return super.getBuilder(converter, command);
    }

    @Override
    protected StepType stepType() {
        return new SwitchToFrameByIndex();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        if (builder.getStepType() instanceof SwitchToFrameByIndex) {
            this.addParam(builder, "index", command.target().replace("index=", ""));
        }
    }
}
