package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepType;

public abstract class AbstractStepType implements StepType {

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }

}
