package com.sebuilder.interpreter.script.seleniumide;

import com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Map;
import java.util.stream.IntStream;

record SeleniumIDECommand(JSONObject source) {

    public String command() {
        return this.source.get("command").toString();
    }

    public String target() {
        return this.source.get("target").toString();
    }

    public String value() {
        return this.source.get("value").toString();
    }

    public Map<String, String> targets() {
        final Map<String, String> result = Maps.newHashMap();
        final JSONArray targets = this.source.getJSONArray("targets");
        IntStream.range(0, targets.length()).forEach(i -> {
            final JSONArray locator = targets.getJSONArray(i);
            result.put(locator.getString(1), locator.getString(0));
        });
        return result;
    }
}
