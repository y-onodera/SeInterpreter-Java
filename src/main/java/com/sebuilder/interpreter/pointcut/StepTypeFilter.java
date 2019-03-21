package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.Step;

import java.util.function.Predicate;

public class StepTypeFilter implements Predicate<Step> {

    private String targetType;

    public StepTypeFilter(String targetType) {
        this.targetType = targetType;
    }

    @Override
    public boolean test(Step step) {
        return targetType.equals(step.getType().getStepTypeName());
    }
}
