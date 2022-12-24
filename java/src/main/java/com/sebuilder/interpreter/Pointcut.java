package com.sebuilder.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public interface Pointcut {

    String METHOD_KEY = "method";

    Map<String, BiFunction<String, String, Boolean>> METHODS = new HashMap<>() {
        {
            this.put("equal", String::equals);
            this.put("startsWith", String::startsWith);
            this.put("endsWith", String::endsWith);
            this.put("matches", String::matches);
            this.put("contains", String::contains);
            this.put("!equal", (a, b) -> !this.get("equal").apply(a, b));
            this.put("!startsWith", (a, b) -> !this.get("startsWith").apply(a, b));
            this.put("!endsWith", (a, b) -> !this.get("endsWith").apply(a, b));
            this.put("!matches", (a, b) -> !this.get("matches").apply(a, b));
            this.put("!contains", (a, b) -> !this.get("contains").apply(a, b));
        }
    };


    Pointcut NONE = new Pointcut() {
        @Override
        public boolean test(final Step step, final InputData vars) {
            return false;
        }

        @Override
        public String toString() {
            return "NONE";
        }
    };

    Pointcut ANY = new Pointcut() {
        @Override
        public boolean test(final Step step, final InputData vars) {
            return true;
        }

        @Override
        public String toString() {
            return "ANY";
        }
    };

    boolean test(Step step, InputData vars);

    default Pointcut or(final Pointcut other) {
        final Pointcut origin = this;
        return new Pointcut() {
            @Override
            public boolean test(final Step step, final InputData vars) {
                return origin.test(step, vars) || other.test(step, vars);
            }

            @Override
            public String toString() {
                return origin + " or " + other.toString();
            }
        };
    }

    default Pointcut and(final Pointcut other) {
        final Pointcut origin = this;
        return new Pointcut() {
            @Override
            public boolean test(final Step step, final InputData vars) {
                return origin.test(step, vars) && other.test(step, vars);
            }

            @Override
            public String toString() {
                return origin + " and " + other.toString();
            }
        };
    }
}
