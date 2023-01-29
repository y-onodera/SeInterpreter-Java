package com.sebuilder.interpreter;

public interface StepRunFilter {
    StepRunFilter ALL_PASS = (step, var) -> true;

    boolean pass(Step step, InputData var);
}
