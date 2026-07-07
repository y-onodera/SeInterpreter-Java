package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.WaitFor;
import com.sebuilder.interpreter.step.getter.ComplexCondition;
import com.sebuilder.interpreter.step.getter.ElementEnable;
import com.sebuilder.interpreter.step.getter.ElementVisible;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.StaleElementReferenceException;

public interface ConditionalStep extends StepType {

    WaitFor WAIT_FOR_ACTIVE = ComplexCondition.builder()
            .addCondition(new ElementVisible())
            .addCondition(new ElementEnable())
            .build()
            .toWaitFor();

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test finish.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    default boolean run(final TestRun ctx) {
        final WaitFor waitForReady = this.waitForReady();
        if (!waitForReady.run(ctx)) {
            return false;
        }
        return this.doRunWithRetry(ctx, waitForReady);
    }

    boolean doRun(TestRun ctx);

    default WaitFor waitForReady() {
        return WAIT_FOR_ACTIVE;
    }

    /**
     * On a fast-rendering page, the element can pass the visible/enabled check above and then be
     * replaced, moved or briefly covered (re-render, animation, overlay) before doRun() actually
     * reaches it, so the very next Selenium call throws even though nothing is really wrong.
     * Retry doRun() against a freshly re-confirmed element instead of failing on that first
     * transient exception, bounded by the same wait budget used for the initial readiness check.
     */
    private boolean doRunWithRetry(final TestRun ctx, final WaitFor waitForReady) {
        int maxWaitMs = Context.getWaitForMaxMs();
        if (ctx.containsKey("maxWait")) {
            maxWaitMs = Integer.parseInt(ctx.string("maxWait"));
        }
        int intervalMs = Context.getWaitForIntervalMs();
        if (ctx.containsKey("interval")) {
            intervalMs = Integer.parseInt(ctx.string("interval"));
        }
        final long stopBy = System.currentTimeMillis() + maxWaitMs;
        while (true) {
            try {
                return this.doRun(ctx);
            } catch (final StaleElementReferenceException | ElementNotInteractableException e) {
                if (ctx.isStopped() || System.currentTimeMillis() >= stopBy) {
                    throw e;
                }
                try {
                    Thread.sleep(intervalMs);
                } catch (final InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw e;
                }
                if (!waitForReady.run(ctx)) {
                    throw e;
                }
            }
        }
    }
}
