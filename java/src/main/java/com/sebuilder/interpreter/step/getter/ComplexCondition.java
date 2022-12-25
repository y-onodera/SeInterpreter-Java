package com.sebuilder.interpreter.step.getter;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.Getter;
import com.sebuilder.interpreter.step.WaitFor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ComplexCondition extends AbstractGetter {
    private final List<WaitFor> conditions = new ArrayList<>();

    public ComplexCondition(final List<WaitFor> conditions) {
        this.conditions.addAll(conditions);
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String get(final TestRun ctx) {
        for (final WaitFor condition : this.conditions) {
            if (!condition.test(ctx)) {
                return "false";
            }
        }
        return "true";
    }

    public static class Builder {
        List<WaitFor> conditions = new ArrayList<>();

        public Builder addCondition(final Getter condition) {
            this.conditions.add(condition.toWaitFor());
            return this;
        }

        public ComplexCondition build() {
            return new ComplexCondition(this.conditions);
        }
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ComplexCondition that = (ComplexCondition) o;
        return Objects.equals(this.conditions, that.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.conditions);
    }
}
