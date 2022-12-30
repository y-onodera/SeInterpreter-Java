package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.step.Getter;

import java.util.Objects;

public abstract class AbstractGetter implements Getter {
    @Override
    public String cmpParamName() {
        return null;
    }

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
        return Objects.hash(this.getClass().getSimpleName());
    }

}
