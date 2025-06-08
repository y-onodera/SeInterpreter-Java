package com.sebuilder.interpreter;

import java.util.Optional;

public enum Strings {
    SINGLETON;

    public static boolean isNotEmpty(String target) {
        return !isEmpty(target);
    }

    public static boolean isEmpty(String target) {
        return Optional.ofNullable(target).filter(it -> !it.isEmpty()).isEmpty();
    }
}
