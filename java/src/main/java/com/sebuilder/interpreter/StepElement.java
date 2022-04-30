package com.sebuilder.interpreter;


public interface StepElement {

    default StepBuilder addDefaultParam(StepBuilder o) {
        return o;
    }

}
