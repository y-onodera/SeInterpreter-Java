package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class Aspect {

    public static Predicate<Step> NONE = new Predicate<Step>() {
        @Override
        public boolean test(Step step) {
            return false;
        }
    };

    public static Predicate<Step> APPLY = new Predicate<Step>() {
        @Override
        public boolean test(Step step) {
            return true;
        }
    };

    private List<Interceptor> interceptors = Lists.newArrayList();

    public Aspect() {
        this(Lists.newArrayList());
    }

    public Aspect(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public Builder builder() {
        return new Builder(this.interceptors);
    }

    public Advice advice(Step step) {
        return new Advice(interceptors.stream()
                .filter(interceptor -> interceptor.isPointcut(step))
                .collect(Collectors.toList()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Aspect aspect = (Aspect) o;
        return Objects.equal(interceptors, aspect.interceptors);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(interceptors);
    }

    public static class Advice {

        private List<Interceptor> advices = Lists.newArrayList();

        public Advice(List<Interceptor> advices) {
            this.advices = advices;
        }

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
    }

    public static class Builder {

        private List<Interceptor> interceptors;

        public Builder(List<Interceptor> interceptors) {
            this.interceptors = interceptors;
        }

        public Interceptor.Builder interceptor() {
            return new Interceptor.Builder(this);
        }

        public Builder add(Interceptor interceptor) {
            this.interceptors.add(interceptor);
            return this;
        }

        public Aspect build() {
            return new Aspect(this.interceptors);
        }
    }
}
