package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

import java.util.HashMap;
import java.util.Map;

public record LocatorFilter(String key, Locator target, String method) implements Pointcut.ExportablePointcut {

    public LocatorFilter(final String key, final Locator target) {
        this(key, target, "equals");
    }

    @Override
    public boolean isHandle(final Step step, final InputData vars) {
        return step.locatorContains(this.key)
                && vars.evaluateString(step.getLocator(this.key).type()).equals(this.target.type())
                && METHODS.get(this.method).apply(vars.evaluateString(step.getLocator(this.key).value()), this.target.value());
    }

    @Override
    public Map<String, String> params() {
        final Map<String, String> result = new HashMap<>();
        result.put("type", this.target.type());
        result.put("value", this.target.value());
        if (!this.method.equals("equals")) {
            result.put("method", this.method);
        }
        return result;
    }
}