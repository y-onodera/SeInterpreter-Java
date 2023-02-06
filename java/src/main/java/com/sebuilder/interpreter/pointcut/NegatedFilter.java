package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

public record NegatedFilter(boolean target) implements Pointcut.Exportable {

    @Override
    public boolean isHandle(final Step step, final InputData vars) {
        return this.target == step.negated();
    }

    @Override
    public String value() {
        return Boolean.toString(this.target);
    }
}
