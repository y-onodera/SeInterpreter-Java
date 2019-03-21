package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.Step;

import java.util.Map;
import java.util.function.Predicate;

public class StringParamFilter implements Predicate<Step> {

    private Map<String, String> targetParam;

    public StringParamFilter(Map<String, String> targetParam) {
        this.targetParam = targetParam;
    }

    @Override
    public boolean test(Step step) {
        for (Map.Entry<String, String> entry : targetParam.entrySet()) {
            if (!match(step, entry)) {
                return false;
            }
        }
        return true;
    }

    private boolean match(Step step, Map.Entry<String, String> entry) {
        return step.containsParam(entry.getKey()) && step.getParam(entry.getKey()).equals(entry.getValue());
    }
}
