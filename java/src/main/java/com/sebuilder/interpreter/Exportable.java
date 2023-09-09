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

    default String toPrettyString() {
        final StringBuilder sb = new StringBuilder().append(this.key()).append(": ").append(this.value());
        this.stringParams().forEach((key, value) -> sb.append(" ").append(key).append("=").append(value));
        this.locatorParams().forEach((key, value) -> sb.append(" ").append(key).append("=").append(value.toPrettyString()));
        return sb.toString();
    }

}
