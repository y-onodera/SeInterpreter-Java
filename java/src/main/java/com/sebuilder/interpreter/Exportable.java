package com.sebuilder.interpreter;

import java.util.HashMap;
import java.util.Map;

public interface Exportable {
    String key();

    default Map<String, String> params() {
        return new HashMap<>();
    }

    default String value() {
        return "";
    }
}
