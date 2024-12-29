package com.sebuilder.interpreter.javafx.view.step;

import org.openqa.selenium.bidi.network.BytesValue;

import java.util.LinkedHashMap;
import java.util.Map;

public record HttpHeaders(LinkedHashMap<String, BytesValue> params) {
    public HttpHeaders() {
        this(new LinkedHashMap<>());
    }

    public HttpHeaders(Map<String, BytesValue> params) {
        this(new LinkedHashMap<>(params));
    }

    public HttpHeaders add(String key, String type, String value) {
        LinkedHashMap<String, BytesValue> newParam = new LinkedHashMap<>(this.params());
        newParam.put(key, new BytesValue(Enum.valueOf(BytesValue.Type.class, type.toUpperCase()), value));
        return new HttpHeaders(newParam);
    }

    public HttpHeaders add(HttpHeaders other) {
        LinkedHashMap<String, BytesValue> newParam = new LinkedHashMap<>(this.params());
        newParam.putAll(other.params());
        return new HttpHeaders(newParam);
    }
}
