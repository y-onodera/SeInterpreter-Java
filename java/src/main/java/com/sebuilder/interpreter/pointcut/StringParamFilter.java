package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.TestRun;

import java.util.HashMap;
import java.util.Map;

public record StringParamFilter(String key, String target, String method) implements Pointcut.ExportablePointcut {

    public StringParamFilter(final String key, final String value) {
        this(key, value, "equals");
    }

    @Override
    public boolean isHandle(final TestRun testRun, final Step step, final InputData vars) {
        return step.containsParam(this.key)
                && METHODS.get(this.method).apply(vars.evaluateString(step.getParam(this.key)), this.target);
    }

    @Override
    public Map<String, String> stringParams() {
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
