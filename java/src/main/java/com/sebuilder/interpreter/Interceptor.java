package com.sebuilder.interpreter;

import java.util.stream.Stream;

public interface Interceptor {

    default Aspect toAspect() {
        return new Aspect().builder().add(this).build();
    }

    default Stream<Interceptor> materialize(final InputData shareInput) {
        return Stream.of(this);
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

    interface ExportableInterceptor extends Interceptor, Exportable {
        @Override
        default String key() {
            return this.getClass().getSimpleName().replace("Interceptor", "").toLowerCase();
        }
    }

}
