package com.sebuilder.interpreter;


public interface StepElement {

    default StepBuilder addDefaultParam(final StepBuilder o) {
        return o;
    }

}
