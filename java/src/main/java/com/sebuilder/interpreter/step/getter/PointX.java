package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.LocatorHolder;

public class PointX extends AbstractGetter implements LocatorHolder {
    @Override
    public String get(TestRun ctx) {
        return String.valueOf(ctx.locator().find(ctx).getRect().getX());
    }

    @Override
    public String cmpParamName() {
        return "at";
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        return o.apply(LocatorHolder.super::addDefaultParam)
                .apply(super::addDefaultParam);
    }
}