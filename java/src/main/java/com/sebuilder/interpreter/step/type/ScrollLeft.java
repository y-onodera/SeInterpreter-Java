package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.step.StaleElementRetry;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.ScrollableTag;
import com.sebuilder.interpreter.screenshot.ScrollableWidth;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;

public class ScrollLeft extends AbstractStepType implements Exportable, LocatorHolder {

    @Override
    public boolean run(final TestRun ctx) {
        // Every attempt finds the element again and sets an absolute value, so a retry is safe.
        return StaleElementRetry.get(() -> this.scroll(ctx));
    }

    private boolean scroll(final TestRun ctx) {
        final ScrollableWidth width;
        if (ctx.hasLocator()) {
            width = ScrollableTag.getWidth(ctx, ctx.locator().find(ctx));
        } else {
            width = Page.getWidth(ctx);
        }
        width.scrollHorizontally(0);
        return true;
    }
}