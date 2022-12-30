package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SetElementSelected;

public class SetElementSelectedConverter extends AbstractStepConverter {

    private final Input input;

    public SetElementSelectedConverter(final Input input) {
        this.input = input;
    }

    @Override
    protected StepType stepType() {
        return new SetElementSelected();
    }

    @Override
    protected void configure(final StepBuilder builder, final SeleniumIDEConverter converter, final SeleniumIDECommand command) {
        if (this.input == Input.SELECT) {
            final String optionPath = String.format("/option[. = '%s']", command.value().replace("label=", ""));
            builder.locator(new Locator("xpath", this.targetToXpath(command) + optionPath));
        } else {
            builder.locator(new Locator("xpath", this.targetToXpath(command)));
        }
    }

    private String targetToXpath(final SeleniumIDECommand command) {
        final String target = command.target();
        if (target.startsWith("id=")) {
            return String.format("//*[@id='%s']", target.replace("id=", ""));
        } else if (target.startsWith("name=")) {
            return String.format("//*[@name='%s']", target.replace("name=", ""));
        } else if (target.startsWith("css=")) {
            return String.format("//*[@css='%s']", target.replace("css=", ""));
        }
        return target.replace("xpath=", "");
    }

    public enum Input {
        SELECT, CHECK
    }
}
