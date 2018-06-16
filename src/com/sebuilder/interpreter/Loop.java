package com.sebuilder.interpreter;

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
        boolean success = true;
        int count = Integer.valueOf(ctx.string("count"));
        for (int i = 0; i < count; i++) {
            ctx.currentStep().stringParams.put("_index", String.valueOf(i + 1));
            success = next(ctx) && success;
        }
        return success;
    }

    private boolean next(TestRun ctx) {
        ctx.toNextStepIndex();
        return ctx.currentStep().type.run(ctx);
    }
}
