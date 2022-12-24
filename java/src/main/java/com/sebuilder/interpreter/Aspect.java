package com.sebuilder.interpreter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

public record Aspect(Collection<Interceptor> interceptors) {

    public Aspect() {
        this(new ArrayList<>());
    }

    public Aspect(Collection<Interceptor> interceptors) {
        this.interceptors = new ArrayList<>(interceptors);
    }

    public Builder builder() {
        return new Builder(this.interceptors);
    }

    public Advice advice(Step step, InputData vars) {
        return new Advice(this.interceptors.stream()
                .filter(interceptor -> interceptor.isPointcut(step, vars))
                .toList());
    }

    public record Advice(List<Interceptor> advices) {

        public boolean invokeBefore(TestRun testRun) {
            for (Interceptor interceptor : this.advices) {
                if (!interceptor.invokeBefore(testRun)) {
                    return false;
                }
            }
            return true;
        }

        public boolean invokeAfter(TestRun testRun) {
            for (Interceptor interceptor : this.advices) {
                if (!interceptor.invokeAfter(testRun)) {
                    return false;
                }
            }
            return true;
        }

        public void invokeFailure(TestRun testRun) {
            for (Interceptor interceptor : this.advices) {
                if (!interceptor.invokeFailure(testRun)) {
                    return;
                }
            }
        }
    }

    public static class Builder {

        private final LinkedHashSet<Interceptor> interceptors;

        public Builder(Collection<Interceptor> interceptors) {
            this.interceptors = new LinkedHashSet<>(interceptors);
        }

        public Interceptor.Builder interceptor() {
            return new Interceptor.Builder(this);
        }

        public Builder add(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder add(Collection<Interceptor> interceptor) {
            this.interceptors.addAll(interceptor);
            return this;
        }

        public Builder add(Aspect other) {
            return this.add(other.interceptors);
        }

        public Aspect build() {
            return new Aspect(this.interceptors);
        }
    }
}