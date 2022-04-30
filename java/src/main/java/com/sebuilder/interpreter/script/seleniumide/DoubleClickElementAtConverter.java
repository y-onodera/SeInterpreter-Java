package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.DoubleClickElementAt;

public class DoubleClickElementAtConverter extends ClickElementAtConverter {

    @Override
    protected StepType stepType() {
        return new DoubleClickElementAt();
    }
}
