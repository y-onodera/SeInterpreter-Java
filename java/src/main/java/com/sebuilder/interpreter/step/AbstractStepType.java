package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.StepType;

public abstract class AbstractStepType implements StepType {

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }

    @Override
    public String toString() {
        return this.getStepTypeName();
    }

}
