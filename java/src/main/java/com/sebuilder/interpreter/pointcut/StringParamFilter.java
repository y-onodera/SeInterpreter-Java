package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

public record StringParamFilter(String key, String value, String method) implements Pointcut {

    public StringParamFilter(String key, String value) {
        this(key, value, "equal");
    }

    @Override
    public boolean test(Step step, InputData vars) {
        return step.containsParam(this.key)
                && METHODS.get(this.method).apply(vars.evaluateString(step.getParam(this.key)), this.value);
    }

}
