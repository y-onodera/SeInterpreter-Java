package com.sebuilder.interpreter.step.getter;

import com.google.common.base.Objects;
import com.sebuilder.interpreter.step.Getter;

public abstract class AbstractGetter implements Getter {
    @Override
    public String cmpParamName() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.getClass().getSimpleName());
    }

}
