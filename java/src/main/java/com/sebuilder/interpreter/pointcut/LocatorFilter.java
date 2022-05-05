package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

import java.util.function.BiFunction;

public class LocatorFilter implements Pointcut {

    private final String key;
    private final Locator target;
    private final String method;

    public LocatorFilter(String key, Locator target) {
        this(key, target, "equal");
    }

    public LocatorFilter(String key, Locator target, String method) {
        this.key = key;
        this.target = target;
        this.method = method;
    }

    @Override
    public boolean test(Step step, InputData vars) {
        return step.locatorContains(this.key)
                && vars.bind(step.getLocator(this.key).type).equals(target.type)
                && METHODS.get(this.method).apply(vars.bind(step.getLocator(this.key).value), target.value);
    }

    @Override
    public String toString() {
        return "LocatorFilter{" +
                "key='" + key + '\'' +
                ", target=" + target +
                ", method=" + method +
                '}';
    }
}