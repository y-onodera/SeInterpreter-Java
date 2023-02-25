package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

public record SkipFilter(boolean target) implements Pointcut.ExportablePointcut {

    @Override
    public boolean isHandle(final TestRun testRun, final Step step, final InputData vars) {
        return step.isSkip(vars) == this.target;
    }

    @Override
    public String value() {
        return Boolean.toString(this.target);
    }

}
