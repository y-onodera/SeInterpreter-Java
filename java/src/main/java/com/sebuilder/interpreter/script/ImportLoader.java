package com.sebuilder.interpreter.script;

import com.google.common.base.Strings;
import org.json.JSONObject;

import java.util.function.BiFunction;

public record ImportLoader() {

    public <R> R load(final JSONObject src, final String key, final BiFunction<String, String, R> loadFunction) {
        if (src.get(key) instanceof String value) {
            return loadFunction.apply(value, "");
        }
        return this.load(src.getJSONObject(key), loadFunction);
    }

    public <R> R load(final JSONObject importObj, final BiFunction<String, String, R> loadFunction) {
        final String pathValue = importObj.getString("path");
        if (importObj.has("where") && !Strings.isNullOrEmpty(importObj.getString("where"))) {
            return loadFunction.apply(pathValue, importObj.getString("where"));
        }
        return loadFunction.apply(pathValue, "");
    }

}
