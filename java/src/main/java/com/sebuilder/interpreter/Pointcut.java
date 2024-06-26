package com.sebuilder.interpreter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;

public interface Pointcut {

    String METHOD_KEY = "method";

    Map<String, BiFunction<String, String, Boolean>> METHODS = new HashMap<>() {
        {
            this.put("equals", String::equals);
            this.put("startsWith", String::startsWith);
            this.put("endsWith", String::endsWith);
            this.put("matches", String::matches);
            this.put("contains", String::contains);
            this.put("!equals", (a, b) -> !this.get("equals").apply(a, b));
            this.put("!startsWith", (a, b) -> !this.get("startsWith").apply(a, b));
            this.put("!endsWith", (a, b) -> !this.get("endsWith").apply(a, b));
            this.put("!matches", (a, b) -> !this.get("matches").apply(a, b));
            this.put("!contains", (a, b) -> !this.get("contains").apply(a, b));
        }
    };

    Pointcut NONE = new Pointcut() {
        @Override
        public boolean isHandle(final TestRun testRun, final Step step, final InputData vars) {
            return false;
        }

        @Override
        public String toString() {
            return "NONE";
        }
    };

    Pointcut ANY = new Pointcut() {
        @Override
        public boolean isHandle(final TestRun testRun, final Step step, final InputData vars) {
            return true;
        }

        @Override
        public String toString() {
            return "ANY";
        }
    };

    boolean isHandle(TestRun testRun, Step step, InputData var);

    default Pointcut materialize(final InputData var) {
        return this;
    }

    default Pointcut or(final Pointcut other) {
        if (this == NONE) {
            return other;
        } else if (other == NONE) {
            return this;
        }
        return new Or(this, other);
    }

    default Pointcut and(final Pointcut other) {
        if (this == ANY) {
            return other;
        } else if (other == ANY) {
            return this;
        }
        return new And(this, other);
    }

    default List<Pointcut> toListTopLevelCondition() {
        final ArrayList<Pointcut> result = new ArrayList<>();
        if (this instanceof Or or) {
            result.addAll(or.origin.toListTopLevelCondition());
            result.addAll(or.other.toListTopLevelCondition());
        } else {
            result.add(this);
        }
        return result;
    }

    default List<Pointcut> getLeafCondition() {
        final ArrayList<Pointcut> result = new ArrayList<>();
        if (this instanceof And and) {
            result.addAll(and.origin.getLeafCondition());
            result.addAll(and.other.getLeafCondition());
        } else {
            result.add(this);
        }
        return result;
    }

    default Pointcut convert(final UnaryOperator<Pointcut> function) {
        return function.apply(this);
    }

    interface ExportablePointcut extends Pointcut, Exportable {
        @Override
        default String key() {
            return this.getClass().getSimpleName().replace("Filter", "").toLowerCase();
        }
    }

    record Or(Pointcut origin, Pointcut other) implements Pointcut {

        @Override
        public boolean isHandle(final TestRun testRun, final Step step, final InputData vars) {
            return this.origin.isHandle(testRun, step, vars) || this.other.isHandle(testRun, step, vars);
        }

        @Override
        public Pointcut materialize(final InputData var) {
            return new Or(this.origin.materialize(var), this.other.materialize(var));
        }

        @Override
        public Pointcut convert(final UnaryOperator<Pointcut> function) {
            return new Pointcut.Or(function.apply(this.origin), function.apply(this.other));
        }

        @Override
        public String toString() {
            return "(" + this.origin + ") or (" + this.other.toString() + ")";
        }

        @Override
        public int hashCode() {
            return this.origin.hashCode() + this.other.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (obj instanceof Or or) {
                return (this.origin.equals(or.origin) && this.other.equals(or.other))
                        || (this.other.equals(or.origin) && this.origin.equals(or.other));
            }
            return false;
        }
    }

    record And(Pointcut origin, Pointcut other) implements Pointcut {

        @Override
        public boolean isHandle(final TestRun testRun, final Step step, final InputData vars) {
            return this.origin.isHandle(testRun, step, vars) && this.other.isHandle(testRun, step, vars);
        }

        @Override
        public Pointcut materialize(final InputData var) {
            return new And(this.origin.materialize(var), this.other.materialize(var));
        }

        @Override
        public Pointcut convert(final UnaryOperator<Pointcut> function) {
            return new Pointcut.And(function.apply(this.origin), function.apply(this.other));
        }

        @Override
        public String toString() {
            return "(" + this.origin + ") and (" + this.other.toString() + ")";
        }

        @Override
        public int hashCode() {
            return this.origin.hashCode() + this.other.hashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (obj instanceof And and) {
                return (this.origin.equals(and.origin) && this.other.equals(and.other))
                        || (this.other.equals(and.origin) && this.origin.equals(and.other));
            }
            return false;
        }
    }
}
