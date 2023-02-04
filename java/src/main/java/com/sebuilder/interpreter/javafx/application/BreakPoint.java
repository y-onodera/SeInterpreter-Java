package com.sebuilder.interpreter.javafx.application;

import com.sebuilder.interpreter.*;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public record BreakPoint(Map<Integer, Pointcut> condition, Debugger debugger) implements Interceptor {

    public static Pointcut STEP_BY_STEP = (step, vars) -> true;

    public static Predicate<Interceptor> typeMatch() {
        return interceptor -> interceptor instanceof BreakPoint;
    }

    public static Predicate<Aspect> isSetting() {
        return it -> it.getStream()
                .anyMatch(typeMatch());
    }

    public static Optional<BreakPoint> findFrom(final Aspect aspect) {
        return aspect.getStream()
                .filter(typeMatch())
                .map(it -> (BreakPoint) it)
                .findFirst();
    }

    @Override
    public boolean isPointcut(final Step step, final InputData vars) {
        switch (this.debugger.getDebugStatus()) {
            case stepOver:
            case stop:
            case pause:
                return STEP_BY_STEP.isHandle(step, vars);
            case await:
            case resume:
        }
        final Integer stepIndex = vars.stepIndex();
        return this.condition.containsKey(stepIndex) && this.condition.get(stepIndex).isHandle(step, vars);
    }

    @Override
    public boolean invokeBefore(final TestRun testRun) {
        return this.debugger.await(testRun);
    }

    @Override
    public boolean isTargetingChain() {
        return false;
    }

    public Collection<Integer> targetStepIndex() {
        return this.condition.keySet();
    }

    public BreakPoint addCondition(final int stepIndex, final Pointcut pointcut) {
        final BreakPoint result = new BreakPoint(new HashMap<>(this.condition), this.debugger);
        result.condition.put(stepIndex, pointcut);
        return result;
    }

    public BreakPoint removeCondition(final int stepIndex) {
        final BreakPoint result = new BreakPoint(new HashMap<>(this.condition), this.debugger);
        result.condition.remove(stepIndex);
        return result;
    }
}
