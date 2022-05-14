package com.sebuilder.interpreter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.Map;

public record Interceptor(Pointcut pointcut,
                          ArrayList<Step> beforeStep,
                          ArrayList<Step> afterStep,
                          ArrayList<Step> failureStep) {
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

    boolean invokeAdvise(TestRun testRun, ArrayList<Step> steps, String testRunName) {
        if (steps.size() == 0) {
            return true;
        }
        final TestCase adviseCase = this.createAdviseCase(steps, testRunName);
        TestRun interceptRun = this.createInterceptorRun(testRun, adviseCase);
        return interceptRun.finish();
    }

    TestCase createAdviseCase(ArrayList<Step> steps, String testRunName) {
        return new TestCaseBuilder()
                .setName(testRunName)
                .addSteps(steps)
                .isShareState(true)
                .build();
    }

    TestRun createInterceptorRun(TestRun testRun, TestCase invokeCase) {
        return new TestRunBuilder(invokeCase.map(it -> it.isPreventContextAspect(true)))
                .addTestRunNamePrefix(this.getInterceptCaseName(testRun))
                .createTestRun(testRun.log(), testRun.driver(), extendsStepVar(testRun), this.createAdviseListener(testRun));
    }

    InputData extendsStepVar(TestRun testRun) {
        Map<String, String> joinStepInfo = Maps.newHashMap();
        testRun.currentStep()
                .toMap()
                .forEach((key, value) -> joinStepInfo.put("_target." + key, value));
        return testRun.vars().add(joinStepInfo).add("_target.currentStepIndex", String.valueOf(testRun.currentStepIndex()));
    }

    TestRunListener createAdviseListener(TestRun testRun) {
        return new TestRunListenerWrapper(testRun.getListener()) {
            String testName;
            @Override
            public boolean openTestSuite(TestCase testCase, String testRunName, InputData aProperty) {
                this.testName = testRunName;
                testRun.log().info("open suite %s".formatted(this.testName));
                return true;
            }

            @Override
            public void closeTestSuite() {
                testRun.log().info("close suite %s".formatted(this.testName));
            }
        };
    }

    String getInterceptCaseName(TestRun testRun) {
        return testRun.getTestRunName() + "_" + testRun.formatStepIndex() + "_" + testRun.currentStep().type().getStepTypeName() + "_";
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
