package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import org.openqa.selenium.interactions.Actions;

public class ClickElementAt extends AbstractStepType {

    @Override
    public boolean run(final TestRun ctx) {
        final int pointY = Integer.parseInt(ctx.string("pointY"));
        final int pointX = Integer.parseInt(ctx.string("pointX"));
        new Actions(ctx.driver())
                .moveByOffset(pointX, pointY)
                .click()
                .build()
                .perform();
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("pointX")) {
            o.put("pointX", "");
        }
        if (!o.containsStringParam("pointY")) {
            o.put("pointY", "");
        }
        return o;
    }

}
