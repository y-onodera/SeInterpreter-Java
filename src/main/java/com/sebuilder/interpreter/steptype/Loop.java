package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.FlowStep;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;

public class Loop implements FlowStep {
    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    public boolean run(TestRun ctx) {
        ctx.processTestSuccess();
        boolean success = true;
        int actions = getSubSteps(ctx);
        int count = Integer.valueOf(ctx.string("count"));
        for (int i = 0; i < count; i++) {
            ctx.putVars("_index", String.valueOf(i + 1));
            success = runSubStep(ctx, success, actions);
            if (!success) {
                ctx.processTestFailure();
                return false;
            }
            if (i + 1 < count) {
                ctx.processTestSuccess();
                ctx.backStepIndex(actions);
            }
        }
        return success;
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        FlowStep.super.supplementSerialized(o);
        if (!o.has("count")) {
            o.put("count", "");
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
