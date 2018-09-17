package com.sebuilder.interpreter.steptype;


import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;

public class Retry implements StepType {

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
        while (!next(ctx)) {
            ctx.processTestFailure();
            for (int i = 0; i < actions; i++) {
                success = next(ctx) && success;
                if (i != actions - 1) {
                    if (success) {
                        ctx.processTestSuccess();
                    } else {
                        ctx.processTestFailure();
                    }
                }
            }
            if (!success) {
                return false;
            }
            ctx.processTestSuccess();
            ctx.backStepIndex(actions + 1);
        }
        ctx.processTestSuccess();
        ctx.forwardStepIndex(actions);
        ctx.startTest();
        return true;
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("subStep")) {
            o.put("subStep", "");
        }
    }

    private boolean next(TestRun ctx) {
        ctx.toNextStepIndex();
        ctx.startTest();
        return ctx.currentStep().type.run(ctx);
    }

}
