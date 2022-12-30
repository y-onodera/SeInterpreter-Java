package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ElementEnable extends AbstractGetter implements LocatorHolder {

    @Override
    public String get(final TestRun ctx) {
        final List<WebElement> result = ctx.locator().findElements(ctx);
        if (result.size() == 0) {
            return "false";
        }
        return "" + result.get(0).isEnabled();
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        return o.apply(LocatorHolder.super::addDefaultParam)
                .apply(super::addDefaultParam);
    }

}
