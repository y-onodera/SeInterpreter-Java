package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.TestRun;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * The polling loop shared by steps that wait for the page (waitFor, ConditionalStep). Reads
 * maxWait / interval from the step, falling back to the {@link Context} defaults, and repeats an
 * attempt until it gives a result, the time runs out or the test run is stopped.
 */
public final class Polling {

    private final TestRun ctx;
    private final int maxWaitMs;
    private final int intervalMs;
    private final List<Class<? extends RuntimeException>> retryOn;

    private Polling(final TestRun ctx, final int maxWaitMs, final int intervalMs,
                    final List<Class<? extends RuntimeException>> retryOn) {
        this.ctx = ctx;
        this.maxWaitMs = maxWaitMs;
        this.intervalMs = intervalMs;
        this.retryOn = retryOn;
    }

    public static Polling from(final TestRun ctx) {
        int maxWaitMs = Context.getWaitForMaxMs();
        if (ctx.containsKey("maxWait")) {
            maxWaitMs = Integer.parseInt(ctx.string("maxWait"));
        }
        int intervalMs = Context.getWaitForIntervalMs();
        if (ctx.containsKey("interval")) {
            intervalMs = Integer.parseInt(ctx.string("interval"));
        }
        return new Polling(ctx, maxWaitMs, intervalMs, List.of());
    }

    /**
     * Exceptions that mean "not ready yet" rather than a failure: an attempt that throws one of
     * them is tried again on the next poll.
     */
    @SafeVarargs
    public final Polling retryOn(final Class<? extends RuntimeException>... exceptions) {
        return new Polling(this.ctx, this.maxWaitMs, this.intervalMs, List.of(exceptions));
    }

    /**
     * @param attempt Returns the step's result once it can decide it, or empty to poll again.
     * @return The attempt's result; false when the time runs out, the test run is stopped or the
     * thread is interrupted.
     * @throws RuntimeException The exception from the last attempt, if that attempt threw one of
     *                          the {@link #retryOn} exceptions and the time then ran out.
     */
    public boolean until(final Supplier<Optional<Boolean>> attempt) {
        final long stopBy = System.currentTimeMillis() + this.maxWaitMs;
        while (true) {
            RuntimeException failure = null;
            try {
                final Optional<Boolean> result = attempt.get();
                if (result.isPresent()) {
                    return result.get();
                }
            } catch (final RuntimeException e) {
                if (this.retryOn.stream().noneMatch(it -> it.isInstance(e))) {
                    throw e;
                }
                failure = e;
            }
            if (this.ctx.isStopped()) {
                // Stopped by the user (e.g. cancelled from the UI): not an error. The driver may
                // already be closing, so avoid another round trip.
                return false;
            }
            if (System.currentTimeMillis() >= stopBy) {
                if (failure != null) {
                    throw failure;
                }
                return false;
            }
            try {
                Thread.sleep(this.intervalMs);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        }
    }
}
