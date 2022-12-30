package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepBuilder;

public abstract class AbstractLocatorStepConverter extends AbstractStepConverter {

    @Override
    protected StepBuilder getBuilder(final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        final StepBuilder result = super.getBuilder(converter, command);
        final String[] locatorTarget = command.target().split("=", 2);
        if (locatorTarget.length == 2) {
            result.locator(new Locator(this.convertLocatorType(locatorTarget[0]), locatorTarget[1]));
        }
        return result;
    }

    protected String convertLocatorType(final String s) {
        return s.replace("css", "css selector")
                .replace("linkText", "link text");
    }

}
