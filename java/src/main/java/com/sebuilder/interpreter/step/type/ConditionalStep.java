package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.util.Optional;

public interface ConditionalStep extends StepType {

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test finish.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    default boolean run(final TestRun ctx) {
        int maxWaitMs = Context.getWaitForMaxMs();
        if (ctx.containsKey("maxWait")) {
            maxWaitMs = Integer.parseInt(ctx.string("maxWait"));
        }
        int intervalMs = Context.getWaitForIntervalMs();
        if (ctx.containsKey("interval")) {
            intervalMs = Integer.parseInt(ctx.string("interval"));
        }
        final long stopBy = System.currentTimeMillis() + maxWaitMs;
        RuntimeException lastFailure = null;
        while (true) {
            final Optional<WebElement> active = this.findActiveElement(ctx);
            if (active.isPresent()) {
                try {
                    return this.doRun(ctx, active.get());
                } catch (final StaleElementReferenceException | NoSuchElementException |
                               ElementNotInteractableException e) {
                    // On a fast-rendering page, the element can pass the check above and then be
                    // replaced, moved or briefly covered (re-render, animation, overlay) before
                    // doRun() actually reaches it. Look it up again instead of failing on that
                    // first transient exception.
                    lastFailure = e;
                }
            }
            if (ctx.isStopped() || System.currentTimeMillis() >= stopBy) {
                if (lastFailure != null) {
                    throw lastFailure;
                }
                return false;
            }
            try {
                Thread.sleep(intervalMs);
            } catch (final InterruptedException ie) {
                Thread.currentThread().interrupt();
                if (lastFailure != null) {
                    throw lastFailure;
                }
                return false;
            }
        }
    }

    /**
     * @param element The element found by the step's locator that is visible and enabled; the same
     *                element the readiness check confirmed.
     */
    boolean doRun(TestRun ctx, WebElement element);

    /**
     * Pick the first element that is visible and enabled, checking both on the same element so that
     * the readiness check and doRun() never look at different elements. An element that goes stale
     * while being inspected is just skipped: it is being re-rendered and is not ready yet.
     */
    default Optional<WebElement> findActiveElement(final TestRun ctx) {
        return ctx.locator().findElements(ctx)
                .stream()
                .filter(ConditionalStep::isActive)
                .findFirst();
    }

    private static boolean isActive(final WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (final StaleElementReferenceException e) {
            return false;
        }
    }
}
