package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import org.openqa.selenium.Dimension;

public class SetWindowSize extends AbstractStepType {
    @Override
    public boolean run(final TestRun ctx) {
        ctx.driver().manage().window().setSize(new Dimension(
                Integer.parseInt(ctx.string("width")),
                Integer.parseInt(ctx.string("height"))));
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("width")) {
            o.put("width", "");
        }
        if (!o.containsStringParam("height")) {
            o.put("height", "");
        }
        return o;
    }
}
