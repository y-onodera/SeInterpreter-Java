package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

public record SkipFilter(boolean target) implements Pointcut {

    @Override
    public boolean isHandle(final Step step, final InputData vars) {
        return step.isSkip(vars) == this.target;
    }

}
