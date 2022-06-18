package com.sebuilder.interpreter;

import com.google.common.collect.Maps;

import java.util.Map;

public record Interceptor(Pointcut pointcut,
                          TestCase beforeStep,
                          TestCase afterStep,
                          TestCase failureStep) {

    public boolean isPointcut(Step step, InputData vars) {
        if (this.pointcut == Pointcut.NONE) {
            return this.beforeStep.steps().size() == 0
                    && this.afterStep.steps().size() == 0
                    && this.failureStep.steps().size() > 0;
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

    boolean invokeAdvise(TestRun testRun, TestCase steps, String testRunName) {
        if (steps.steps().size() == 0) {
            return true;
        }
        final TestCase adviseCase = this.createAdviseCase(steps, testRunName);
        TestRun interceptRun = this.createInterceptorRun(testRun, adviseCase);
        return interceptRun.finish();
    }

    TestCase createAdviseCase(TestCase steps, String testRunName) {
        return steps.builder()
                .setName(testRunName)
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

        private TestCase beforeStep = new TestCaseBuilder().build();

        private TestCase afterStep = new TestCaseBuilder().build();

        private TestCase failureStep = new TestCaseBuilder().build();

        public Builder(Aspect.Builder builder) {
            aspectBuilder = builder;
        }

        public Builder setPointcut(Pointcut pointcut) {
            this.pointcut = pointcut;
            return this;
        }

        public Builder addBefore(TestCase testCase) {
            this.beforeStep = testCase;
            return this;
        }

        public Builder addAfter(TestCase testCase) {
            this.afterStep = testCase;
            return this;
        }

        public Builder addFailure(TestCase testCase) {
            this.failureStep = testCase;
            return this;
        }

        public Aspect.Builder build() {
            return this.aspectBuilder.add(new Interceptor(this.pointcut, this.beforeStep, this.afterStep, this.failureStep));
        }
    }
}
