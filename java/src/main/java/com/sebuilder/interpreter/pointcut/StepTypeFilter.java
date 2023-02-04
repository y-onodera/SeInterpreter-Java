package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

public record StepTypeFilter(String target, String method) implements Pointcut {

    public StepTypeFilter(final String targetType) {
        this(targetType, "equal");
    }

    @Override
    public boolean isHandle(final Step step, final InputData vars) {
        return METHODS.get(this.method).apply(step.type().getStepTypeName(), this.target);
    }

}
