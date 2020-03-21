package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.ScrollableTag;
import com.sebuilder.interpreter.screenshot.ScrollableWidth;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;

public class ScrollHorizontally extends AbstractStepType implements Exportable, LocatorHolder {

    @Override
    public boolean run(TestRun ctx) {
        ScrollableWidth width;
        if(ctx.hasLocator()) {
            width = ScrollableTag.getWidth(ctx, ctx.locator().find(ctx));
        }else {
            width = Page.getWidth(ctx);
        }
        width.scrollHorizontally(Integer.parseInt(ctx.string("pointX")));
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("pointX")) {
            o.put("pointX", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }
}
