package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

public record NegatedFilter(boolean target) implements Pointcut {

    @Override
    public boolean test(final Step step, final InputData vars) {
        return this.target == step.negated();
    }

}
