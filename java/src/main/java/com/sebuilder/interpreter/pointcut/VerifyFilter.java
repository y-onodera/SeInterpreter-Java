package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.step.Verify;

import java.util.Map;

public record VerifyFilter(Verify verify,
                           Map<String, String> stringCondition,
                           Map<String, Locator> locatorCondition) implements Pointcut.ExportablePointcut {

    @Override
    public boolean isHandle(final TestRun testRun, final Step step, final InputData var) {
        final TestRun ctx = new TestRunBuilder(this.toStep().toTestCase())
                .createTestRun(testRun.varWithCurrentStepInfo(), testRun, testRun.currentStepIndex());
        ctx.forwardStepIndex(1);
        return this.verify.test(ctx);
    }

    public Step toStep() {
        final StepBuilder verifyStep = this.verify.toStep();
        this.stringCondition.entrySet().stream()
                .filter(it -> !it.getKey().equals("negated"))
                .forEach(entry -> verifyStep.put(entry.getKey(), entry.getValue()));
        this.locatorCondition.forEach(verifyStep::put);
        if (this.stringCondition.containsKey("negated")) {
            verifyStep.negated(Boolean.parseBoolean(this.stringCondition.get("negated")));
        }
        return verifyStep.build();
    }

    @Override
    public String key() {
        return this.verify.getStepTypeName();
    }

    @Override
    public Map<String, String> stringParams() {
        final Map<String, String> result = ExportablePointcut.super.stringParams();
        result.putAll(this.stringCondition());
        return result;
    }

    @Override
    public Map<String, Locator> locatorParams() {
        final Map<String, Locator> result = ExportablePointcut.super.locatorParams();
        result.putAll(this.locatorCondition());
        return result;
    }

}
