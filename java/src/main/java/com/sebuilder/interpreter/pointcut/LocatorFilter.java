package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

public record LocatorFilter(String key, Locator target, String method) implements Pointcut {

    public LocatorFilter(final String key, final Locator target) {
        this(key, target, "equal");
    }

    @Override
    public boolean isHandle(final Step step, final InputData vars) {
        return step.locatorContains(this.key)
                && vars.evaluateString(step.getLocator(this.key).type()).equals(this.target.type())
                && METHODS.get(this.method).apply(vars.evaluateString(step.getLocator(this.key).value()), this.target.value());
    }

}