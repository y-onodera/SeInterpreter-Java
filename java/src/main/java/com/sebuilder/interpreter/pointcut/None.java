package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.Step;

import java.util.function.Predicate;

public class None implements Predicate<Step> {
    @Override
    public boolean test(Step step) {
        return false;
    }
}
