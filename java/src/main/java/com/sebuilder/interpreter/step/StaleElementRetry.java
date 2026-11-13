package com.sebuilder.interpreter.step;

import org.openqa.selenium.StaleElementReferenceException;

import java.util.function.Supplier;

/**
 * Retries an operation on elements when one goes stale. On a page that re-renders, an element can
 * be replaced between findElements() and the next call on it, and the driver can even throw from
 * findElements() itself while the DOM is changing. The operation must look its elements up again on
 * every attempt (never reuse an element reference from outside), and be safe to run again from the
 * start: StaleElementReferenceException means the call on the stale element did nothing.
 */
public final class StaleElementRetry {

    public static final int RETRY_COUNT = 3;
    public static final int RETRY_INTERVAL_MS = 100;

    private StaleElementRetry() {
    }

    /**
     * Runs the operation up to {@link #RETRY_COUNT} times, waiting {@link #RETRY_INTERVAL_MS}
     * between attempts. Rethrows the last exception if every attempt is stale.
     */
    public static <T> T get(final Supplier<T> operation) {
        StaleElementReferenceException lastFailure = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            if (i > 0) {
                try {
                    Thread.sleep(RETRY_INTERVAL_MS);
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            try {
                return operation.get();
            } catch (final StaleElementReferenceException e) {
                lastFailure = e;
            }
        }
        throw lastFailure;
    }
}
