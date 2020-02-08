package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepBuilder;
import org.json.JSONException;

public abstract class AbstractLocatorStepConverter extends AbstractStepConverter {

    @Override
    protected StepBuilder getBuilder(SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        StepBuilder result = super.getBuilder(converter, command);
        String[] locatorTarget = command.target().split("=", 2);
        if (locatorTarget.length == 2) {
            result.locator(new Locator(convertLocatorType(locatorTarget[0]), locatorTarget[1]));
        }
        return result;
    }

    protected String convertLocatorType(String s) {
        return s.replace("css", "css selector")
                .replace("linkText", "link text");
    }

}
