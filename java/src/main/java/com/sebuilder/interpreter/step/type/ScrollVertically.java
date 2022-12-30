package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.ScrollableHeight;
import com.sebuilder.interpreter.screenshot.ScrollableTag;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;

public class ScrollVertically extends AbstractStepType implements Exportable, LocatorHolder {

    @Override
    public boolean run(final TestRun ctx) {
        final ScrollableHeight height;
        if (ctx.hasLocator()) {
            height = ScrollableTag.getHeight(ctx, ctx.locator().find(ctx));
        } else {
            height = Page.getHeight(ctx);
        }
        height.scrollVertically(Integer.parseInt(ctx.string("pointY")));
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("pointY")) {
            o.put("pointY", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }
}

