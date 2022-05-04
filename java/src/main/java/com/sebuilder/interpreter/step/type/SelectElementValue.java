package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public class SelectElementValue extends AbstractStepType implements ConditionalStep, LocatorHolder {

    @Override
    public boolean doRun(TestRun ctx) {
        WebElement e = ctx.locator().find(ctx);
        Select select = new Select(e);
        select.selectByValue(ctx.string("value"));
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("value")) {
            o.put("value", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

}

