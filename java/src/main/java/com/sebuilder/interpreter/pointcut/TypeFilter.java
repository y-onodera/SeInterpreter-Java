package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

import java.util.HashMap;
import java.util.Map;

public record TypeFilter(String target, String method) implements Pointcut.ExportablePointcut {

    public TypeFilter(final String targetType) {
        this(targetType, "equals");
    }

    @Override
    public boolean isHandle(final TestRun testRun, final Step step, final InputData vars) {
        return METHODS.get(this.method).apply(step.type().getStepTypeName(), this.target);
    }

    @Override
    public Map<String, String> params() {
        final Map<String, String> result = new HashMap<>();
        if (!this.method.equals("equals")) {
            result.put("value", this.target);
            result.put("method", this.method);
        }
        return result;
    }

    @Override
    public String value() {
        if (this.method.equals("equals")) {
            return this.target;
        }
        return ExportablePointcut.super.value();
    }
}
