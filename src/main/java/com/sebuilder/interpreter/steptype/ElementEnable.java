package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.Getter;
import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.WebElement;

import java.util.List;

public class ElementEnable implements Getter {

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
}
