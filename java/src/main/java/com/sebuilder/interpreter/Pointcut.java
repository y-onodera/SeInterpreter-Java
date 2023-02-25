package com.sebuilder.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

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
        return new Or(this, other);
    }

    default Pointcut and(final Pointcut other) {
        return new And(this, other);
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
