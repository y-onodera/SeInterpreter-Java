package com.sebuilder.interpreter;

import com.google.common.collect.Maps;

import java.util.Map;
import java.util.function.Supplier;

public record ExtraStepExecuteInterceptor(Pointcut pointcut,
                                          TestCase beforeStep,
                                          TestCase afterStep,
                                          TestCase failureStep) implements Interceptor {

    @Override
    public boolean isPointcut(final Step step, final InputData vars) {
        if (this.pointcut == Pointcut.NONE) {
            return this.beforeStep.steps().size() == 0
                    && this.afterStep.steps().size() == 0
                    && this.failureStep.steps().size() > 0;
        }
        return this.pointcut.isHandle(step, vars);
    }

    @Override
    public boolean invokeBefore(final TestRun testRun) {
        return this.invokeAdvise(testRun, this.beforeStep, "before");
    }

    @Override
    public boolean invokeAfter(final TestRun testRun) {
        return this.invokeAdvise(testRun, this.afterStep, "after");
    }

    @Override
    public boolean invokeFailure(final TestRun testRun) {
        return this.invokeAdvise(testRun, this.failureStep, "failure");
    }

    boolean invokeAdvise(final TestRun testRun, final TestCase steps, final String testRunName) {
        if (steps.steps().size() == 0) {
            return true;
        }
        final TestCase adviseCase = this.createAdviseCase(steps, testRunName);
        final TestRun interceptRun = this.createInterceptorRun(testRun, adviseCase);
        return interceptRun.finish();
    }

    TestCase createAdviseCase(final TestCase steps, final String testRunName) {
        return steps.builder()
                .setName(testRunName)
                .isShareState(true)
                .build();
    }

    TestRun createInterceptorRun(final TestRun testRun, final TestCase invokeCase) {
        return new TestRunBuilder(invokeCase.map(it -> it.isPreventContextAspect(true)))
                .addTestRunNamePrefix(this.getInterceptCaseName(testRun))
                .createTestRun(testRun.log(), testRun.driver(), this.extendsStepVar(testRun), this.createAdviseListener(testRun));
    }

    InputData extendsStepVar(final TestRun testRun) {
        final Map<String, String> joinStepInfo = Maps.newHashMap();
        testRun.currentStep()
                .toMap()
                .forEach((key, value) -> joinStepInfo.put("_target." + key, value));
        return testRun.vars().add(joinStepInfo).add("_target.currentStepIndex", String.valueOf(testRun.currentStepIndex()));
    }

    TestRunListener createAdviseListener(final TestRun testRun) {
        return new TestRunListenerWrapper(testRun.getListener()) {
            String testName;

            @Override
            public boolean openTestSuite(final TestCase testCase, final String testRunName, final InputData aProperty) {
                this.testName = testRunName;
                this.isAspectRunning(true);
                testRun.log().info("open suite %s".formatted(this.testName));
                return true;
            }

            @Override
            public void closeTestSuite() {
                testRun.log().info("close suite %s".formatted(this.testName));
                this.isAspectRunning(false);
            }
        };
    }

    String getInterceptCaseName(final TestRun testRun) {
        return testRun.getTestRunName() + "_" + testRun.formatStepIndex() + "_" + testRun.currentStep().type().getStepTypeName() + "_";
    }


    public static class Builder implements Supplier<Interceptor> {
        private Pointcut pointcut = Pointcut.NONE;

        private TestCase beforeStep = new TestCaseBuilder().build();

        private TestCase afterStep = new TestCaseBuilder().build();

        private TestCase failureStep = new TestCaseBuilder().build();

        public Builder setPointcut(final Pointcut pointcut) {
            this.pointcut = pointcut;
            return this;
        }

        public Builder addBefore(final TestCase testCase) {
            this.beforeStep = testCase;
            return this;
        }

        public Builder addAfter(final TestCase testCase) {
            this.afterStep = testCase;
            return this;
        }

        public Builder addFailure(final TestCase testCase) {
            this.failureStep = testCase;
            return this;
        }

        @Override
        public Interceptor get() {
            return new ExtraStepExecuteInterceptor(this.pointcut, this.beforeStep, this.afterStep, this.failureStep);
        }
    }
}
