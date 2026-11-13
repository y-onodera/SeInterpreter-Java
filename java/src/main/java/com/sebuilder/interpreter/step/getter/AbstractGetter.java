package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.step.Getter;
import com.sebuilder.interpreter.step.StaleElementRetry;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractGetter implements Getter {

    /**
     * Run a lookup that finds elements and inspects them, looking the elements up again when one
     * goes stale. See {@link StaleElementRetry}.
     */
    protected static String retryOnStale(final Supplier<String> lookup) {
        return StaleElementRetry.get(lookup);
    }

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
