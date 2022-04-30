package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;

import java.util.Map;
import java.util.function.Predicate;

public class LocatorFilter implements Predicate<Step> {

    private final Map<String, Locator> targetParam;

    public LocatorFilter(Map<String, Locator> targetParam) {
        this.targetParam = targetParam;
    }

    @Override
    public boolean test(Step step) {
        for (Map.Entry<String, Locator> entry : targetParam.entrySet()) {
            if (!match(step, entry)) {
                return false;
            }
        }
        return true;
    }

    private boolean match(Step step, Map.Entry<String, Locator> entry) {
        return step.locatorContains(entry.getKey()) && step.getLocator(entry.getKey()).equals(entry.getValue());
    }
}