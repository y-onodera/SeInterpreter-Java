package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

import java.util.function.BiFunction;

public class StepTypeFilter implements Pointcut {
    private final String targetType;
    private final String method;

    public StepTypeFilter(String targetType) {
        this(targetType, "equal");
    }

    public StepTypeFilter(String value, String method) {
        this.targetType = value;
        this.method = method;
    }

    @Override
    public boolean test(Step step, InputData vars) {
        return METHODS.get(this.method).apply(step.getType().getStepTypeName(), this.targetType);
    }

    @Override
    public String toString() {
        return "StepTypeFilter{" +
                "targetType='" + targetType + '\'' +
                ", method=" + method +
                '}';
    }
}
