package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.WaitFor;

public interface ConditionalStep extends StepType {

    WaitFor waitForActive = ComplexCondition.builder()
            .addCondition(new ElementVisible())
            .addCondition(new ElementEnable())
            .build()
            .toWaitFor();

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
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

    default WaitFor waitForReady(){
            return waitForActive;
    }
}
