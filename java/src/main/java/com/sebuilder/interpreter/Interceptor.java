package com.sebuilder.interpreter;

import java.util.HashMap;
import java.util.Map;
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

    interface Exportable extends Interceptor {
        default String key() {
            return this.getClass().getSimpleName().replace("Interceptor", "").toLowerCase();
        }

        default Map<String, String> params() {
            return new HashMap<>();
        }

        default String value() {
            return "";
        }

    }

}
