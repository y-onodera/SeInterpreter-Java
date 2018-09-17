package com.sebuilder.interpreter.javafx.event.replay;

import java.util.function.Function;
import java.util.function.Predicate;

public class RunStepEvent {

    private final Predicate<Number> filter;

    private final Function<Integer, Integer> stepNoFunction;

    public RunStepEvent(Predicate<Number> filter, Function<Integer, Integer> stepNoFunction) {
        this.filter = filter;
        this.stepNoFunction = stepNoFunction;
    }

    public Predicate<Number> getFilter() {
        return filter;
    }

    public Function<Integer, Integer> getStepNoFunction() {
        return stepNoFunction;
    }
}
