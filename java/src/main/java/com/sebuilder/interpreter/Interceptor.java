package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Interceptor {
    private final Pointcut pointcut;

    private final ArrayList<Step> beforeStep;

    private final ArrayList<Step> afterStep;

    private final ArrayList<Step> failureStep;

    public Interceptor(Pointcut pointcut, ArrayList<Step> beforeStep, ArrayList<Step> afterStep, ArrayList<Step> failureStep) {
        this.pointcut = pointcut;
        this.beforeStep = Lists.newArrayList(beforeStep);
        this.afterStep = Lists.newArrayList(afterStep);
        this.failureStep = Lists.newArrayList(failureStep);
    }

    public boolean isPointcut(Step step, InputData vars) {
        if (this.pointcut == Pointcut.NONE) {
            return this.beforeStep.size() == 0 && this.afterStep.size() == 0 && this.failureStep.size() > 0;
        }
        return this.pointcut.test(step, vars);
    }

    public boolean invokeBefore(TestRun testRun) {
        return this.invokeAdvise(testRun, this.beforeStep, "before");
    }

    public boolean invokeAfter(TestRun testRun) {
        return this.invokeAdvise(testRun, this.afterStep, "after");
    }

    public boolean invokeFailure(TestRun testRun) {
        return this.invokeAdvise(testRun, this.failureStep, "failure");
    }

    protected boolean invokeAdvise(TestRun testRun, ArrayList<Step> steps, String testRunName) {
        if (steps.size() == 0) {
            return true;
        }
        final TestCase adviseCase = this.createAdviseCase(steps, testRunName);
        TestRun interceptRun = this.createInterceptorRun(testRun, adviseCase);
        return interceptRun.finish();
    }

    protected TestCase createAdviseCase(ArrayList<Step> steps, String testRunName) {
        return new TestCaseBuilder()
                .setName(testRunName)
                .addSteps(steps)
                .isShareState(true)
                .build();
    }

    protected TestRun createInterceptorRun(TestRun testRun, TestCase invokeCase) {
        return new TestRunBuilder(invokeCase.map(it -> it.isPreventContextAspect(true)))
                .addTestRunNamePrefix(this.getInterceptCaseName(testRun))
                .createTestRun(testRun.log(), testRun.driver(), extendsStepVar(testRun), this.createAdviseListener(testRun));
    }

    protected InputData extendsStepVar(TestRun testRun) {
        Map<String, String> joinStepInfo = Maps.newHashMap();
        testRun.currentStep()
                .toMap()
                .entrySet()
                .forEach(it -> joinStepInfo.put("target." + it.getKey(), it.getValue()));
        return testRun.vars().add(joinStepInfo).add("target.currentStepIndex", String.valueOf(testRun.currentStepIndex()));
    }

    protected TestRunListener createAdviseListener(TestRun testRun) {
        return testRun.getListener().copy();
    }

    protected String getInterceptCaseName(TestRun testRun) {
        return testRun.getTestRunName() + "_" + testRun.currentStep().getType().getStepTypeName() + "_aspect_";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interceptor that = (Interceptor) o;
        return Objects.equal(this.pointcut, that.pointcut) &&
                Objects.equal(this.beforeStep, that.beforeStep) &&
                Objects.equal(this.afterStep, that.afterStep) &&
                Objects.equal(this.failureStep, that.failureStep);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.pointcut, this.beforeStep, this.afterStep, this.failureStep);
    }

    public static class Builder {
        private final Aspect.Builder aspectBuilder;
        private Pointcut pointcut = Pointcut.NONE;

        private final ArrayList<Step> beforeStep = Lists.newArrayList();

        private final ArrayList<Step> afterStep = Lists.newArrayList();

        private final ArrayList<Step> failureStep = Lists.newArrayList();

        public Builder(Aspect.Builder builder) {
            aspectBuilder = builder;
        }

        public Builder setPointcut(Pointcut pointcut) {
            this.pointcut = pointcut;
            return this;
        }

        public Builder addBefore(ArrayList<Step> before) {
            this.beforeStep.addAll(before);
            return this;
        }

        public Builder addAfter(ArrayList<Step> after) {
            this.afterStep.addAll(after);
            return this;
        }

        public Builder addFailure(ArrayList<Step> failure) {
            this.failureStep.addAll(failure);
            return this;
        }

        public Aspect.Builder build() {
            return this.aspectBuilder.add(new Interceptor(this.pointcut, this.beforeStep, this.afterStep, this.failureStep));
        }
    }
}
