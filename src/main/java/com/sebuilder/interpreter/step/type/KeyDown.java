package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;

public class KeyDown extends AbstractStepType {

    @Override
    public boolean run(TestRun ctx) {
        ctx.driver().getKeyboard().pressKey(ctx.text());
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("text")) {
            o.put("text", "");
        }
        return o;
    }

}
