package com.sebuilder.interpreter;

public interface Interceptor {
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
}
