package com.sebuilder.interpreter.step;

import com.google.common.base.Objects;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;

public class If implements FlowStep, GetterUseStep {

    private final Getter getter;

    public If(Getter getter) {
        this.getter = getter;
    }

    @Override
    public Getter getGetter() {
        return getter;
    }

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    public boolean run(TestRun ctx) {
        ctx.processTestSuccess();
        boolean success = true;
        int actions = getSubSteps(ctx);
        if (this.test(ctx)) {
            success = this.runSubStep(ctx, success, actions);
            if (!success) {
                ctx.processTestFailure();
                return false;
            }
        } else {
            ctx.forwardStepIndex(actions);
        }
        return true;
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        FlowStep.super.supplementSerialized(o);
        GetterUseStep.super.supplementSerialized(o);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        If anIf = (If) o;
        return Objects.equal(getGetter(), anIf.getGetter());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getGetter());
    }
}
