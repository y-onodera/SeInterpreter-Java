package com.sebuilder.interpreter;

import org.json.JSONException;
import org.json.JSONObject;

public class Print implements StepType {
    public final Getter getter;

    public Print(Getter getter) {
        this.getter = getter;
    }

    @Override
    public boolean run(TestRun ctx) {
        String value = getter.get(ctx);
        ctx.log().info(value);
        return true;
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        getter.supplementSerialized(o);
    }
}
