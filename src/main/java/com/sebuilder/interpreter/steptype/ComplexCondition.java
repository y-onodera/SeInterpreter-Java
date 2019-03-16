package com.sebuilder.interpreter.steptype;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.Getter;
import com.sebuilder.interpreter.step.WaitFor;

import java.util.List;

public class ComplexCondition implements Getter {
    private final List<WaitFor> conditions = Lists.newArrayList();

    public ComplexCondition(List<WaitFor> conditions) {
        this.conditions.addAll(conditions);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * @param ctx Current test run.
     * @return The value this getter gets, eg the page title.
     */
    @Override
    public String get(TestRun ctx) {
        for (WaitFor condition : this.conditions) {
            if (!condition.run(ctx)) {
                return "false";
            }
        }
        return "true";
    }

    /**
     * @return The name of the parameter to compare the result of the get to, or null if the get
     * returns a boolean "true"/"false".
     */
    @Override
    public String cmpParamName() {
        return null;
    }

    public static class Builder {
        List<WaitFor> conditions = Lists.newArrayList();

        public Builder addCondition(Getter condition) {
            conditions.add(condition.toWaitFor());
            return this;
        }

        public ComplexCondition build() {
            return new ComplexCondition(conditions);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComplexCondition that = (ComplexCondition) o;
        return Objects.equal(conditions, that.conditions);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(conditions);
    }
}
