package com.sebuilder.interpreter.javafx.view.step;

import com.sebuilder.interpreter.BytesValueSource;
import org.openqa.selenium.bidi.network.BytesValue;

import java.util.LinkedHashMap;
import java.util.Map;

public record HttpHeaders(LinkedHashMap<String, BytesValueSource> params) {
    public HttpHeaders() {
        this(new LinkedHashMap<>());
    }

    public HttpHeaders(Map<String, BytesValueSource> params) {
        this(new LinkedHashMap<>(params));
    }

    public HttpHeaders add(String key, String type, String value, String filePath, boolean needEncoding) {
        LinkedHashMap<String, BytesValueSource> newParam = new LinkedHashMap<>(this.params());
        newParam.put(key, new BytesValueSource(Enum.valueOf(BytesValue.Type.class, type.toUpperCase())
                , value, filePath, needEncoding));
        return new HttpHeaders(newParam);
    }

    public HttpHeaders add(HttpHeaders other) {
        LinkedHashMap<String, BytesValueSource> newParam = new LinkedHashMap<>(this.params());
        newParam.putAll(other.params());
        return new HttpHeaders(newParam);
    }
}
