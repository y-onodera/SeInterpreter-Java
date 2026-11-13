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
    private static final int RETRY_INTERVAL_MS = 100;

    private StaleElementRetry() {
    }

    /**
     * Runs the operation up to {@link #RETRY_COUNT} times, waiting {@link #RETRY_INTERVAL_MS}
     * between attempts. Rethrows the last exception if every attempt is stale (an interrupted
     * thread counts as running out of attempts, the same as in {@link Polling}).
     */
    public static <T> T get(final Supplier<T> operation) {
        StaleElementReferenceException lastFailure = null;
        for (int i = 0; i < RETRY_COUNT; i++) {
            if (i > 0 && !sleep(RETRY_INTERVAL_MS)) {
                break;
            }
            try {
                return operation.get();
            } catch (final StaleElementReferenceException e) {
                lastFailure = e;
            }
        }
        throw lastFailure;
    }

    /**
     * Sleeps between attempts. An interrupt is the caller's cue to give up: it is passed on (the
     * thread stays interrupted) and the caller treats it as running out of time or attempts.
     *
     * @return false if the thread was interrupted.
     */
    static boolean sleep(final long ms) {
        try {
            Thread.sleep(ms);
            return true;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
}
