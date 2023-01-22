package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.*;

import java.util.function.Predicate;

public record BreakPoint(Pointcut condition, Debugger debugger) implements Interceptor {

    public static Pointcut STEP_BY_STEP = (step, vars) -> true;
    public static Pointcut DO_NOT_BREAK = (step, var) -> false;

    public static Predicate<Aspect> isSetting() {
        return it -> it.interceptors()
                .stream()
                .anyMatch(interceptor -> interceptor instanceof BreakPoint);
    }

    public BreakPoint addNewStep(final Pointcut other) {
        return new BreakPoint(this.condition.or(other), this.debugger);
    }

    @Override
    public boolean isPointcut(final Step step, final InputData vars) {
        switch (this.debugger.getDebugStatus()) {
            case stepOver:
            case stop:
            case pause:
                return STEP_BY_STEP.test(step, vars);
            case await:
            case resume:
        }
        return this.condition.test(step, vars);
    }

    @Override
    public boolean invokeBefore(final TestRun testRun) {
        return this.debugger.await(testRun);
    }

    @Override
    public boolean isTargetingChain() {
        return false;
    }
}
