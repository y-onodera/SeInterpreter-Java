package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

import java.util.function.BiFunction;

public class StepTypeFilter implements Pointcut {
    private final String targetType;
    private final BiFunction<String, String, Boolean> strategy;

    public StepTypeFilter(String targetType) {
        this(targetType, "equal");
    }

    public StepTypeFilter(String value, String strategy) {
        this.targetType = value;
        this.strategy = STRATEGIES.get(strategy);
    }

    @Override
    public boolean test(Step step, InputData vars) {
        return strategy.apply(targetType, step.getType().getStepTypeName());
    }

}
