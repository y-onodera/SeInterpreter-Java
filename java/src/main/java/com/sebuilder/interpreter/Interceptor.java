package com.sebuilder.interpreter;

public interface Interceptor {

    default Aspect toAspect() {
        return new Aspect().builder().add(this).build();
    }

    default Interceptor materialize(final InputData shareInput) {
        return this;
    }

    default boolean isPointcut(final Step step, final InputData vars) {
        return false;
    }

    default boolean invokeBefore(final TestRun testRun) {
        return true;
    }

    default boolean invokeAfter(final TestRun testRun) {
        return true;
    }

    default boolean invokeFailure(final TestRun testRun) {
        return true;
    }

    default boolean isTargetingChain() {
        return true;
    }

}
