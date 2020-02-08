package com.sebuilder.interpreter.script.seleniumide;

import com.google.common.collect.Maps;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

class SeleniumIDECommand {
    private final JSONObject source;

    public SeleniumIDECommand(JSONObject source) {
        this.source = source;
    }

    public String command() throws JSONException {
        return this.source.get("command").toString();
    }

    public String target() throws JSONException {
        return this.source.get("target").toString();
    }

    public String value() throws JSONException {
        return this.source.get("value").toString();
    }

    public Map<String, String> targets() throws JSONException {
        Map<String, String> result = Maps.newHashMap();
        JSONArray targets = this.source.getJSONArray("targets");
        for (int i = 0, j = targets.length(); i < j; i++) {
            JSONArray locator = targets.getJSONArray(i);
            result.put(locator.getString(1), locator.getString(0));
        }
        return result;
    }
}
