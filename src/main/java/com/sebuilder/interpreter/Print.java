package com.sebuilder.interpreter;

import com.google.common.base.Objects;
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
        if (ctx.currentStep().isNegated()) {
            value = String.valueOf(!Boolean.valueOf(value));
        }
        ctx.log().info(value);
        return true;
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        getter.supplementSerialized(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Print print = (Print) o;
        return Objects.equal(getter, print.getter);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getter);
    }
}
