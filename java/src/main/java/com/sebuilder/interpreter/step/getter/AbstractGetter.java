package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.step.Getter;
import org.openqa.selenium.StaleElementReferenceException;

import java.util.Objects;
import java.util.function.Supplier;

public abstract class AbstractGetter implements Getter {

    static final int STALE_RETRY_COUNT = 3;
    static final int STALE_RETRY_INTERVAL_MS = 100;

    /**
     * Run a lookup that finds elements and inspects them, looking the elements up again when one
     * goes stale. On a page that re-renders, an element can be replaced between findElements() and
     * isDisplayed()/isEnabled(), and the driver can even throw from findElements() itself while the
     * DOM is changing. Each attempt starts again from findElements(), so it never reuses an element
     * reference that has already gone stale. Rethrows the last exception if every attempt is stale.
     */
    protected static String retryOnStale(final Supplier<String> lookup) {
        StaleElementReferenceException lastFailure = null;
        for (int i = 0; i < STALE_RETRY_COUNT; i++) {
            if (i > 0) {
                try {
                    Thread.sleep(STALE_RETRY_INTERVAL_MS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            try {
                return lookup.get();
            } catch (final StaleElementReferenceException e) {
                lastFailure = e;
            }
        }
        throw lastFailure;
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
