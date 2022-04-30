package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepElement;

public interface LocatorHolder extends StepElement {

    default StepBuilder addDefaultParam(StepBuilder o) {
        return this.addDefaultParam("locator", o);
    }

    default StepBuilder addDefaultParam(String key, StepBuilder o) {
        if (!o.containsLocatorParam(key)) {
            o.put(key, new Locator("id", ""));
        }
        return o;
    }
}
