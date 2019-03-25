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

    private boolean invokeAdvise(TestRun testRun, ArrayList<Step> steps, String testRunName) {
        final TestCase invokeCase = new TestCaseBuilder()
                .setName(testRunName)
                .addSteps(steps)
                .usePreviousDriverAndVars(true)
                .build();
        TestRun interceptRun = new TestRunBuilder(invokeCase, new Scenario(invokeCase))
                .addTestRunNamePrefix(this.getInterceptCaseName(testRun))
                .createTestRun(testRun.log(), testRun.driver(), testRun.vars(), new SeInterpreterTestListenerImpl(testRun.getListener()));
        return interceptRun.finish();
    }

    private String getInterceptCaseName(TestRun testRun) {
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