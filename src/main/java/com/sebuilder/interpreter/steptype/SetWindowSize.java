package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Dimension;

public class SetWindowSize implements StepType {
    @Override
    public boolean run(TestRun ctx) {
        ctx.driver().manage().window().setSize(new Dimension(
                Integer.parseInt(ctx.string("width")),
                Integer.parseInt(ctx.string("height"))));
        return true;
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("width")) {
            o.put("width", "");
        }
        if (!o.has("height")) {
            o.put("height", "");
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
