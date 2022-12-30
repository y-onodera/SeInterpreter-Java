package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepElement;

public interface LocatorHolder extends StepElement {

    @Override
    default StepBuilder addDefaultParam(final StepBuilder o) {
        return this.addDefaultParam("locator", o);
    }

    default StepBuilder addDefaultParam(final String key, final StepBuilder o) {
        if (!o.containsLocatorParam(key)) {
            o.put(key, new Locator("id", ""));
        }
        return o;
    }
}
