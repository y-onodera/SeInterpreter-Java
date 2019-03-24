package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepElement;

public interface LocatorHolder extends StepElement {

    default StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsLocatorParam("locator")) {
            o.put("locator", new Locator("id", ""));
        }
        return o;
    }
}
