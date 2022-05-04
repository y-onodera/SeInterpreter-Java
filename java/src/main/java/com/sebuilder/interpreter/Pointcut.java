package com.sebuilder.interpreter;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public interface Pointcut {
    Map<String, BiFunction<String, String, Boolean>> STRATEGIES = new HashMap<>() {
        {
            put("equal", String::equals);
            put("startsWith", String::startsWith);
            put("endsWith", String::endsWith);
            put("matches", String::matches);
            put("contains", String::contains);
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
