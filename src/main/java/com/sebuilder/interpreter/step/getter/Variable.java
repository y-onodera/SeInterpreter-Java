package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

public class Variable extends AbstractGetter {

    @Override
    public String get(TestRun ctx) {
        String variableName = ctx.string("variable");
        return ctx.bindRuntimeVariables("${" + variableName + "}");
    }

    @Override
    public String cmpParamName() {
        return "value";
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("variable")) {
            o.put("variable", "");
        }
        return o.apply(super::addDefaultParam);
    }

}
