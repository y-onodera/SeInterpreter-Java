package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.Getter;
import org.json.JSONException;
import org.json.JSONObject;

public class Variable implements Getter {
    /**
     * @param ctx Current test run.
     * @return The value this getter gets, eg the page title.
     */
    @Override
    public String get(TestRun ctx) {
        String variableName = ctx.string("variable");
        ctx.currentStep().put("text", "${" + variableName + "}");
        return ctx.text();
    }

    /**
     * @return The name of the parameter to compare the result of the get to, or null if the get
     * returns a boolean "true"/"false".
     */
    @Override
    public String cmpParamName() {
        return "value";
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("variable")) {
            o.put("variable", "");
        }
        if (!o.has("text")) {
            o.put("text", "");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
