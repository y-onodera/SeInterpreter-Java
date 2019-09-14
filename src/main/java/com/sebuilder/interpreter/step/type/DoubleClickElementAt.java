package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.interactions.Actions;

public class DoubleClickElementAt extends AbstractStepType {

    @Override
    public boolean run(TestRun ctx) {
        int pointY = Integer.valueOf(ctx.string("pointY")).intValue();
        int pointX = Integer.valueOf(ctx.string("pointX")).intValue();
        new Actions(ctx.driver())
                .moveByOffset(pointX, pointY)
                .doubleClick()
                .build()
                .perform();
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("pointX")) {
            o.put("pointX", "");
        }
        if (!o.containsStringParam("pointY")) {
            o.put("pointY", "");
        }
        return o;
    }
}

