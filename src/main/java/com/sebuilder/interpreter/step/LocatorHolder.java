package com.sebuilder.interpreter.step;

import com.sebuilder.interpreter.JSONSerializable;
import org.json.JSONException;
import org.json.JSONObject;

public interface LocatorHolder extends JSONSerializable {
    @Override
    default void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("locator")) {
            JSONObject locator = new JSONObject();
            locator.put("type", "");
            locator.put("value", "");
            o.put("locator", locator);
        }
    }
}
