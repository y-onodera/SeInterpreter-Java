package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.TestRun;

import java.util.Optional;
import java.util.function.Supplier;

/**
 * Polls the page until an attempt gives the step's result. Reads maxWait / interval from the step,
 * falling back to the {@link Context} defaults.
 */
public final class Polling {

    private Polling() {
    }

    /**
     * @param attempt Returns the step's result once it can decide it, or empty to poll again.
     * @param retryOn Exceptions that mean "not ready yet" rather than a failure: an attempt that
     *                throws one of them is tried again on the next poll.
     * @return The attempt's result; false when the time runs out, the test run is stopped or the
     * thread is interrupted.
     * @throws RuntimeException The exception from the last attempt, if that attempt threw one of
     *                          the retryOn exceptions and the time then ran out.
     */
    @SafeVarargs
    public static boolean until(final TestRun ctx, final Supplier<Optional<Boolean>> attempt,
                                final Class<? extends RuntimeException>... retryOn) {
        final int intervalMs = ctx.getInt("interval", Context.getWaitForIntervalMs());
        final long stopBy = System.currentTimeMillis() + ctx.getInt("maxWait", Context.getWaitForMaxMs());
        while (true) {
            RuntimeException failure = null;
            try {
                final Optional<Boolean> result = attempt.get();
                if (result.isPresent()) {
                    return result.get();
                }
            } catch (final RuntimeException e) {
                if (!isAnyOf(e, retryOn)) {
                    throw e;
                }
                failure = e;
            }
            if (ctx.isStopped()) {
                // Stopped by the user (e.g. cancelled from the UI): not an error. The driver may
                // already be closing, so avoid another round trip.
                return false;
            }
            final long remainingMs = stopBy - System.currentTimeMillis();
            if (remainingMs <= 0) {
                if (failure != null) {
                    throw failure;
                }
                return false;
            }
            try {
                Thread.sleep(Math.min(intervalMs, remainingMs));
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }

    private static boolean isAnyOf(final RuntimeException e, final Class<? extends RuntimeException>[] types) {
        for (final Class<? extends RuntimeException> type : types) {
            if (type.isInstance(e)) {
                return true;
            }
        }
        return false;
    }
}
