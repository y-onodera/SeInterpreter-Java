package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;

public class Variable extends AbstractGetter {

    @Override
    public String get(final TestRun ctx) {
        return ctx.bindRuntimeVariables("${" + ctx.string("variable") + "}");
    }

    @Override
    public String cmpParamName() {
        return "value";
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("variable")) {
            o.put("variable", "");
        }
        return o.apply(super::addDefaultParam);
    }

}
