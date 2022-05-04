package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

import java.util.function.Predicate;

public class SkipFilter implements Pointcut {

    private final boolean target;

    public SkipFilter(boolean target) {
        this.target = target;
    }

    @Override
    public boolean test(Step step, InputData vars) {
        return step.isSkip(vars) == target;
    }
}
