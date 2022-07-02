package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.WaitFor;
import com.sebuilder.interpreter.step.getter.ComplexCondition;
import com.sebuilder.interpreter.step.getter.ElementEnable;
import com.sebuilder.interpreter.step.getter.ElementVisible;

public interface ConditionalStep extends StepType {

    WaitFor WAIT_FOR_ACTIVE = ComplexCondition.builder()
            .addCondition(new ElementVisible())
            .addCondition(new ElementEnable())
            .build()
            .toWaitFor();

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test finish.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    default boolean run(TestRun ctx) {
        WaitFor waitForReady = waitForReady();
        if (waitForReady.run(ctx)) {
            return doRun(ctx);
        }
        return false;
    }

    boolean doRun(TestRun ctx);

    default WaitFor waitForReady() {
        return WAIT_FOR_ACTIVE;
    }
}
