package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.Getter;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ElementEnable implements Getter, LocatorHolder {

    /**
     * @param ctx Current test run.
     * @return The value this getter gets, eg the page title.
     */
    @Override
    public String get(TestRun ctx) {
        List<WebElement> result = ctx.locator().findElements(ctx);
        if (result.size() == 0) {
            return "false";
        }
        return "" + result.get(0).isEnabled();
    }

    /**
     * @return The name of the parameter to compare the result of the get to, or null if the get
     * returns a boolean "true"/"false".
     */
    @Override
    public String cmpParamName() {
        return null;
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        return o.apply(LocatorHolder.super::addDefaultParam)
                .apply(Getter.super::addDefaultParam);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
