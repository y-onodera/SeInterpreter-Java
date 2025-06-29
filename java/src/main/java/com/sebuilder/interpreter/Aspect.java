package com.sebuilder.interpreter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public record Aspect(Iterable<Interceptor> interceptors) implements Iterable<Interceptor> {

    public static Aspect from(final Stream<Interceptor> interceptors) {
        return new Aspect(interceptors.collect(Collectors.toList()));
    }

    public Aspect() {
        this(new ArrayList<>());
    }

    public Aspect(final Iterable<Interceptor> interceptors) {
        final ArrayList<Interceptor> src = new ArrayList<>();
        interceptors.forEach(src::add);
        this.interceptors = src;
    }

    public Builder builder() {
        return new Builder(this.interceptors);
    }

    public Advice advice(final TestRun testRun, final Step step, final InputData vars) {
        return new Advice(this.getStream()
                .filter(interceptor -> interceptor.isPointcut(testRun, step, vars))
                .toList());
    }

    public boolean contains(final Interceptor target) {
        return this.getStream().anyMatch(interceptor -> interceptor.equals(target));
    }

    public Stream<Interceptor> getStream() {
        return StreamSupport.stream(this.interceptors.spliterator(), false);
    }

    @Override
    public Iterator<Interceptor> iterator() {
        return this.interceptors.iterator();
    }

    public Aspect filter(final Predicate<Interceptor> condition) {
        return from(this.getStream().filter(condition));
    }

    public Aspect materialize(final InputData shareInput) {
        return from(this.getStream().flatMap(it -> it.materialize(shareInput)));
    }

    public Aspect takeOverChain(final boolean newValue) {
        return from(this.getStream().map(it -> it.takeOverChain(newValue)));
    }

    public Aspect replace(final Interceptor currentValue, final Interceptor newValue) {
        return this.convert(it -> currentValue.equals(it) ? newValue : it);
    }

    public Aspect remove(final Interceptor removeItem) {
        return from(this.getStream().filter(it -> !it.equals(removeItem)));
    }

    public Aspect remove(final Predicate<Interceptor> filter) {
        return from(this.getStream().filter(it -> !filter.test(it)));
    }

    public Aspect convert(final UnaryOperator<Interceptor> function) {
        return from(this.getStream().map(function));
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

        public Builder(final Iterable<Interceptor> interceptors) {
            this.interceptors = new LinkedHashSet<>();
            interceptors.forEach(this.interceptors::add);
        }

        public Builder add(final Supplier<Interceptor> interceptorSupplier) {
            this.interceptors.add(interceptorSupplier.get());
            return this;
        }

        public Builder add(final Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Builder add(final Iterable<Interceptor> interceptor) {
            interceptor.forEach(this.interceptors::add);
            return this;
        }

        public Builder insert(final Iterable<Interceptor> other) {
            final LinkedHashSet<Interceptor> newInterceptors = new LinkedHashSet<>();
            other.forEach(newInterceptors::add);
            newInterceptors.addAll(this.interceptors);
            this.interceptors = newInterceptors;
            return this;
        }

        public Aspect build() {
            return new Aspect(this.interceptors);
        }

    }
}