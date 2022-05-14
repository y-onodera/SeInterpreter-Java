package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

public record StepTypeFilter(String target, String method) implements Pointcut {

    public StepTypeFilter(String targetType) {
        this(targetType, "equal");
    }

    @Override
    public boolean test(Step step, InputData vars) {
        return METHODS.get(this.method).apply(step.type().getStepTypeName(), this.target);
    }

}
