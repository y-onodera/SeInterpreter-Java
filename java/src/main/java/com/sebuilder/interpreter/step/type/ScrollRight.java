package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.ScrollableTag;
import com.sebuilder.interpreter.screenshot.ScrollableWidth;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;

public class ScrollRight extends AbstractStepType implements Exportable, LocatorHolder {

    @Override
    public boolean run(final TestRun ctx) {
        final ScrollableWidth width;
        if (ctx.hasLocator()) {
            width = ScrollableTag.getWidth(ctx, ctx.locator().find(ctx));
        } else {
            width = Page.getWidth(ctx);
        }
        width.scrollHorizontally(width.getScrollWidth());
        return true;
    }
}