package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Pointcut;
import com.sebuilder.interpreter.Step;

import java.util.function.BiFunction;

public class StringParamFilter implements Pointcut {

    private final String key;
    private final String value;
    private final BiFunction<String, String, Boolean> method;


    public StringParamFilter(String key, String value) {
        this(key, value, "equal");
    }

    public StringParamFilter(String key, String value, String method) {
        this.key = key;
        this.value = value;
        this.method = METHODS.get(method);
    }

    @Override
    public boolean test(Step step, InputData vars) {
        return step.containsParam(this.key)
                && this.method.apply(vars.bind(step.getParam(this.key)), this.value);
    }

}
