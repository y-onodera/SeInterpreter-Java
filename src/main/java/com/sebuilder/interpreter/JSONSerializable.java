package com.sebuilder.interpreter;

import org.json.JSONException;
import org.json.JSONObject;

public interface JSONSerializable {
    default void supplementSerialized(JSONObject o) throws JSONException {
        // nothing
    }
}
