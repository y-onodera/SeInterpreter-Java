package com.sebuilder.interpreter;

import java.util.HashMap;
import java.util.Map;

public interface Exportable {
    String key();

    default Map<String, String> stringParams() {
        return new HashMap<>();
    }

    default Map<String, Locator> locatorParams() {
        return new HashMap<>();
    }

    default String value() {
        return "";
    }
}
