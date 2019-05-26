package com.sebuilder.interpreter;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.function.Predicate;

public class Interceptor {
    private final Predicate<Step> pointcut;

    private final ArrayList<Step> beforeStep;

    private final ArrayList<Step> afterStep;

    public Interceptor(Predicate<Step> pointcut, ArrayList<Step> beforeStep, ArrayList<Step> afterStep) {
        this.pointcut = pointcut;
        this.beforeStep = Lists.newArrayList(beforeStep);
        this.afterStep = Lists.newArrayList(afterStep);
    }

    public boolean isPointcut(Step step) {
        return this.pointcut.test(step);
    }

    public boolean invokeBefore(TestRun testRun) {
        return this.invokeAdvise(testRun, this.beforeStep, "before");
    }

    public boolean invokeAfter(TestRun testRun) {
        return this.invokeAdvise(testRun, this.afterStep, "after");
    }

    protected boolean invokeAdvise(TestRun testRun, ArrayList<Step> steps, String testRunName) {
        final TestCase adviseCase = this.createAdviseCase(steps, testRunName);
        TestRun interceptRun = this.createInterceptorRun(testRun, adviseCase);
        return interceptRun.finish();
    }

    protected TestCase createAdviseCase(ArrayList<Step> steps, String testRunName) {
        return new TestCaseBuilder()
                .setName(testRunName)
                .addSteps(steps)
                .usePreviousDriverAndVars(true)
                .build();
    }

    protected TestRun createInterceptorRun(TestRun testRun, TestCase invokeCase) {
        return new TestRunBuilder(invokeCase, new Scenario(invokeCase))
                .addTestRunNamePrefix(this.getInterceptCaseName(testRun))
                .preventContextAspect(true)
                .createTestRun(testRun.log(), testRun.driver(), testRun.vars(), this.createAdviseListener(testRun));
    }

    protected TestRunListenerImpl createAdviseListener(TestRun testRun) {
        return new TestRunListenerImpl(testRun.getListener());
    }

    protected String getInterceptCaseName(TestRun testRun) {
        return testRun.getTestRunName() + "_" + testRun.currentStep().getType().getStepTypeName() + "_aspect_";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Interceptor that = (Interceptor) o;
        return Objects.equal(pointcut, that.pointcut) &&
                Objects.equal(beforeStep, that.beforeStep) &&
                Objects.equal(afterStep, that.afterStep);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(pointcut, beforeStep, afterStep);
    }

    public static class Builder {
        private final Aspect.Builder aspectBuilder;
        private Predicate<Step> pointcut = Aspect.NONE;

        private ArrayList<Step> beforeStep = Lists.newArrayList();

        private ArrayList<Step> afterStep = Lists.newArrayList();

        public Builder(Aspect.Builder builder) {
            aspectBuilder = builder;
        }

        public Builder setPointcut(Predicate<Step> pointcut) {
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

        public Aspect.Builder build() {
            return this.aspectBuilder.add(new Interceptor(this.pointcut, this.beforeStep, this.afterStep));
        }
    }
}
