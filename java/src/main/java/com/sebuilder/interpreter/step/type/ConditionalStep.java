package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.Polling;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;

import java.util.Objects;
import java.util.Optional;

public interface ConditionalStep extends StepType {

    int DEFAULT_STABLE_WAIT_MS = 100;

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test finish.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    default boolean run(final TestRun ctx) {
        final int stableWaitMs = ctx.getInt("stableWait", DEFAULT_STABLE_WAIT_MS);
        // On a fast-rendering page, the element can pass the readiness check and then be replaced,
        // moved or briefly covered (re-render, animation, overlay) before doRun() actually reaches
        // it. Look it up again on the next poll instead of failing on that transient exception.
        return Polling.until(ctx,
                () -> this.findActiveElement(ctx)
                        .filter(element -> isStable(element, stableWaitMs))
                        .map(element -> this.doRun(ctx, element)),
                StaleElementReferenceException.class, NoSuchElementException.class,
                ElementNotInteractableException.class);
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
        try {
            return ctx.locator().findElements(ctx)
                    .stream()
                    .filter(ConditionalStep::isActive)
                    .findFirst();
        } catch (final StaleElementReferenceException e) {
            // The driver can throw from findElements() itself while the DOM is changing.
            return Optional.empty();
        }
    }

    /**
     * An element in the middle of an animation (slide-in, expanding menu, scroll into place) is
     * visible and enabled but an action on it lands somewhere else or is intercepted. Treat it as
     * ready only once its position and size are the same across stableWaitMs; 0 or less skips it.
     */
    private static boolean isStable(final WebElement element, final int stableWaitMs) {
        if (stableWaitMs <= 0) {
            return true;
        }
        try {
            final Rectangle before = element.getRect();
            Thread.sleep(stableWaitMs);
            return Objects.equals(before, element.getRect());
        } catch (final StaleElementReferenceException e) {
            return false;
        } catch (final InterruptedException e) {
            Thread.currentThread().interrupt();
            return true;
        }
    }

    private static boolean isActive(final WebElement element) {
        try {
            return element.isDisplayed() && element.isEnabled();
        } catch (final StaleElementReferenceException e) {
            return false;
        }
    }
}
