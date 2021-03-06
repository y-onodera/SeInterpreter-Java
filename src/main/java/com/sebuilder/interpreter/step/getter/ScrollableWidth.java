package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.LocatorHolder;

public class ScrollableWidth extends AbstractGetter implements LocatorHolder {
    @Override
    public String get(TestRun ctx) {
        if (ctx.hasLocator()) {
            return String.valueOf(ctx.locator().find(ctx).getCssValue("scrollWidth"));
        }
        return String.valueOf(ctx.getContentWidth());
    }

    @Override
    public String cmpParamName() {
        return "length";
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        return o.apply(LocatorHolder.super::addDefaultParam)
                .apply(super::addDefaultParam);
    }
}

