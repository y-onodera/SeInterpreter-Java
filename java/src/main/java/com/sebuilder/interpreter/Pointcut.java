package com.sebuilder.interpreter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public interface Pointcut {

    String METHOD_KEY = "method";

    Map<String, BiFunction<String, String, Boolean>> METHODS = new HashMap<>() {
        {
            put("equal", String::equals);
            put("startsWith", String::startsWith);
            put("endsWith", String::endsWith);
            put("matches", String::matches);
            put("contains", String::contains);
            put("!equal", (a, b) -> !get("equal").apply(a, b));
            put("!startsWith", (a, b) -> !get("startsWith").apply(a, b));
            put("!endsWith", (a, b) -> !get("endsWith").apply(a, b));
            put("!matches", (a, b) -> !get("matches").apply(a, b));
            put("!contains", (a, b) -> !get("contains").apply(a, b));
        }
    };


    Pointcut NONE = new Pointcut() {
        @Override
        public boolean test(Step step, InputData vars) {
            return false;
        }

        @Override
        public String toString() {
            return "NONE";
        }
    };

    Pointcut ANY = new Pointcut() {
        @Override
        public boolean test(Step step, InputData vars) {
            return true;
        }

        @Override
        public String toString() {
            return "ANY";
        }
    };

    boolean test(Step step, InputData vars);

    default Pointcut or(Pointcut other) {
        Pointcut origin = this;
        return new Pointcut() {
            @Override
            public boolean test(Step step, InputData vars) {
                return origin.test(step, vars) || other.test(step, vars);
            }

            @Override
            public String toString() {
                return origin + " or " + other.toString();
            }
        };
    }

    default Pointcut and(Pointcut other) {
        Pointcut origin = this;
        return new Pointcut() {
            @Override
            public boolean test(Step step, InputData vars) {
                return origin.test(step, vars) && other.test(step, vars);
            }

            @Override
            public String toString() {
                return origin + " and " + other.toString();
            }
        };
    }
}
