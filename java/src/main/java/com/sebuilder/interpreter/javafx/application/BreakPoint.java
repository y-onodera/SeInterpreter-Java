package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Step;

import java.util.function.BiFunction;

public interface BreakPoint extends BiFunction<Step, InputData, Boolean> {

    BreakPoint STEP_BY_STEP = (step, vars) -> true;
    BreakPoint DO_NOT_BREAK = (step, var) -> false;

}
