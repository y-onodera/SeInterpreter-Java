package com.sebuilder.interpreter;

import java.util.function.Supplier;
import java.util.stream.Stream;

public record ExtraStepExecutor(Pointcut pointcut,
                                TestCase beforeStep,
                                TestCase afterStep,
                                TestCase failureStep,
                                boolean takeOverChain) implements Interceptor {

    public ExtraStepExecutor(final Pointcut pointcut,
                             final TestCase beforeStep,
                             final TestCase afterStep,
                             final TestCase failureStep) {
        this(pointcut, beforeStep, afterStep, failureStep, true);
    }

    @Override
    public Stream<Interceptor> materialize(final InputData shareInput) {
        return Stream.of(new ExtraStepExecutor(this.pointcut.materialize(shareInput)
                , this.beforeStep.mapWhen(TestCase::isLazyLoad, it -> it.setShareInput(shareInput).build().execLazyLoad().builder())
                , this.afterStep.mapWhen(TestCase::isLazyLoad, it -> it.setShareInput(shareInput).build().execLazyLoad().builder())
                , this.failureStep.mapWhen(TestCase::isLazyLoad, it -> it.setShareInput(shareInput).build().execLazyLoad().builder())
                , this.takeOverChain
        ));
    }

    @Override
    public boolean isPointcut(final TestRun testRun, final Step step, final InputData vars) {
        if (this.pointcut == Pointcut.NONE) {
            return this.beforeStep.steps().size() == 0
                    && this.afterStep.steps().size() == 0
                    && this.failureStep.steps().size() > 0;
        }
        return this.pointcut.isHandle(testRun, step, vars);
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

    @Override
    public boolean isTakeOverChain() {
        return this.takeOverChain;
    }

    @Override
    public Interceptor takeOverChain(final boolean newValue) {
        if (newValue == this.takeOverChain) {
            return this;
        }
        return new ExtraStepExecutor(this.pointcut, this.beforeStep, this.afterStep, this.failureStep, newValue);
    }

    public TestRunListener createAdviseListener(final TestRun testRun) {
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
                .createTestRun(testRun.log(), testRun.driver(), testRun.varWithCurrentStepInfo(), this.createAdviseListener(testRun));
    }

    String getInterceptCaseName(final TestRun testRun) {
        return testRun.getTestRunName() + "_" + testRun.formatStepIndex() + "_" + testRun.currentStep().type().getStepTypeName() + "_";
    }


    public static class Builder implements Supplier<Interceptor> {
        private Pointcut pointcut = Pointcut.NONE;

        private TestCase beforeStep = new TestCaseBuilder().build();

        private TestCase afterStep = new TestCaseBuilder().build();

        private TestCase failureStep = new TestCaseBuilder().build();

        private boolean takeOverChain = true;

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

        public Builder setTakeOverChain(final boolean takeOverChain) {
            this.takeOverChain = takeOverChain;
            return this;
        }

        @Override
        public Interceptor get() {
            return new ExtraStepExecutor(this.pointcut, this.beforeStep, this.afterStep, this.failureStep, this.takeOverChain);
        }

    }
}
