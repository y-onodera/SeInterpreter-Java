package com.sebuilder.interpreter.script.seleniumide;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.type.DoubleClickElement;

public class DoubleClickElementConverter extends ClickElementConverter{
    @Override
    protected StepType stepType() {
        return new DoubleClickElement();
    }
}
