package com.sebuilder.interpreter;

import java.util.stream.Stream;

public interface Interceptor {

    default Aspect toAspect() {
        return new Aspect().builder().add(this).build();
    }

    default Stream<Interceptor> materialize(final InputData shareInput) {
        return Stream.of(this);
    }

    default boolean isPointcut(final TestRun testRun, final Step step, final InputData vars) {
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

    boolean isTakeOverChain();

    Interceptor takeOverChain(boolean newValue);

    interface ExportableInterceptor extends Interceptor, Exportable {
        @Override
        default String key() {
            return this.getClass().getSimpleName().replace("Interceptor", "").toLowerCase();
        }
    }

}
