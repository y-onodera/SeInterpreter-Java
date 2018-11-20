package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;

public class Loop implements StepType {
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
        int actions = Integer.valueOf(ctx.string("subStep"));
        int count = Integer.valueOf(ctx.string("count"));
        for (int i = 0; i < count; i++) {
            success = runOneStep(ctx, success, actions, i);
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

    public boolean runOneStep(TestRun ctx, boolean success, int actions, int i) {
        ctx.vars().put("_index", String.valueOf(i + 1));
        for (int exec = 0; exec < actions; exec++) {
            success = next(ctx) && success;
            if (exec != actions - 1) {
                if (success) {
                    ctx.processTestSuccess();
                }
            }
        }
        return success;
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("subStep")) {
            o.put("subStep", "");
        }
        if (!o.has("count")) {
            o.put("count", "");
        }
    }

    private boolean next(TestRun ctx) {
        return ctx.runTest();
    }
}
