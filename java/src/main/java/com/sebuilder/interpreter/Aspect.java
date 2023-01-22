package com.sebuilder.interpreter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Supplier;

public record Aspect(Collection<Interceptor> interceptors) {

    public Aspect() {
        this(new ArrayList<>());
    }

    public Aspect(final Collection<Interceptor> interceptors) {
        this.interceptors = new ArrayList<>(interceptors);
    }

    public Builder builder() {
        return new Builder(this.interceptors);
    }

    public Advice advice(final Step step, final InputData vars) {
        return new Advice(this.interceptors.stream()
                .filter(interceptor -> interceptor.isPointcut(step, vars))
                .toList());
    }

    public record Advice(List<Interceptor> advices) {

        public boolean invokeBefore(final TestRun testRun) {
            for (final Interceptor interceptor : this.advices) {
                if (!interceptor.invokeBefore(testRun)) {
                    return false;
                }
            }
            return true;
        }

        public boolean invokeAfter(final TestRun testRun) {
            for (final Interceptor interceptor : this.advices) {
                if (!interceptor.invokeAfter(testRun)) {
                    return false;
                }
            }
            return true;
        }

        public void invokeFailure(final TestRun testRun) {
            for (final Interceptor interceptor : this.advices) {
                if (!interceptor.invokeFailure(testRun)) {
                    return;
                }
            }
        }
    }

    public static class Builder {

        private LinkedHashSet<Interceptor> interceptors;

        public Builder(final Collection<Interceptor> interceptors) {
            this.interceptors = new LinkedHashSet<>(interceptors);
        }

        public Builder add(final Supplier<Interceptor> interceptorSupplier) {
            this.interceptors.add(interceptorSupplier.get());
            return this;
        }

        public Builder add(final Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder add(final Collection<Interceptor> interceptor) {
            this.interceptors.addAll(interceptor);
            return this;
        }

        public Builder add(final Aspect other) {
            return this.add(other.interceptors);
        }

        public Builder insert(final Aspect other) {
            final LinkedHashSet<Interceptor> newInterceptors = new LinkedHashSet<>();
            newInterceptors.addAll(other.interceptors);
            newInterceptors.addAll(this.interceptors);
            this.interceptors = newInterceptors;
            return this;
        }

        public Aspect build() {
            return new Aspect(this.interceptors);
        }

    }
}