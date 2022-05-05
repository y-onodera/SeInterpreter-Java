package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

public class NegatedFilter implements Pointcut {

    private final boolean target;

    public NegatedFilter(boolean target) {
        this.target = target;
    }

    @Override
    public boolean test(Step step, InputData vars) {
        return target == step.isNegated();
    }

    @Override
    public String toString() {
        return "NegatedFilter{" +
                "target=" + target +
                '}';
    }
}
