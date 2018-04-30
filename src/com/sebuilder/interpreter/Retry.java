package com.sebuilder.interpreter;


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
        boolean success = true;
        int actions = Integer.valueOf(ctx.string("subStep"));
        while (!next(ctx)) {
            for (int i = 0; i < actions; i++) {
                success = next(ctx) && success;
            }
            if (!success) {
                return false;
            }
            ctx.backStepIndex(actions + 1);
        }
        ctx.forwardStepIndex(actions);
        return true;
    }

    private boolean next(TestRun ctx) {
        ctx.toNextStepIndex();
        return ctx.currentStep().type.run(ctx);
    }

}
