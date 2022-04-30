package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.SetElementSelected;
import org.json.JSONException;

public class SetElementSelectedConverter extends AbstractStepConverter {

    private final Input input;

    public SetElementSelectedConverter(Input input) {
        this.input = input;
    }

    @Override
    protected StepType stepType() {
        return new SetElementSelected();
    }

    @Override
    protected void configure(StepBuilder builder, SeleniumIDEConverter converter, SeleniumIDECommand command) throws JSONException {
        if (this.input == Input.SELECT) {
            String optionPath = String.format("/option[. = '%s']", command.value().replace("label=", ""));
            builder.locator(new Locator("xpath", targetToXpath(command) + optionPath));
        } else {
            builder.locator(new Locator("xpath", targetToXpath(command)));
        }
    }

    private String targetToXpath(SeleniumIDECommand command) throws JSONException {
        String target = command.target();
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
