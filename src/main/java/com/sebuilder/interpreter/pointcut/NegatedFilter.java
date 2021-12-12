package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.Step;

import java.util.function.Predicate;

public class NegatedFilter implements Predicate<Step> {

    private final boolean target;

    public NegatedFilter(boolean target) {
        this.target = target;
    }

    @Override
    public boolean test(Step step) {
        return target == step.isNegated();
    }
}
