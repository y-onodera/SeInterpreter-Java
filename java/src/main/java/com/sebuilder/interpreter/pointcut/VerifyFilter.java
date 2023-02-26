package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.step.LocatorHolder;
import com.sebuilder.interpreter.step.Verify;

import java.util.Map;

public record VerifyFilter(boolean handleNoLocator, Verify verify,
                           Map<String, String> param) implements Pointcut.ExportablePointcut {

    @Override
    public boolean isHandle(final TestRun testRun, final Step step, final InputData var) {
        if (this.verify.getter instanceof LocatorHolder && step.locatorParams().size() == 0) {
            return this.handleNoLocator;
        }
        final StepBuilder verifyStep = this.verify.toStep();
        step.locatorParams().forEach(verifyStep::put);
        this.param.forEach(verifyStep::put);
        if (this.param.containsKey("negated")) {
            verifyStep.negated(Boolean.parseBoolean(this.param.get("negated")));
        }
        final TestRun ctx = new TestRunBuilder(verifyStep.build().toTestCase())
                .createTestRun(var, testRun, testRun.currentStepIndex());
        ctx.forwardStepIndex(1);
        return this.verify.test(ctx);
    }

    @Override
    public String key() {
        return this.verify.getStepTypeName();
    }

    @Override
    public Map<String, String> params() {
        final Map<String, String> result = ExportablePointcut.super.params();
        if (this.verify.getter instanceof LocatorHolder) {
            result.put("handleNoLocator", String.valueOf(this.handleNoLocator));
        }
        result.putAll(this.param());
        return result;
    }
}
