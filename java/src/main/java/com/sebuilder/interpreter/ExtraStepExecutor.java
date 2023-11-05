package com.sebuilder.interpreter;

import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public record ExtraStepExecutor(Pointcut pointcut,
                                TestCase beforeStep,
                                TestCase afterStep,
                                TestCase failureStep,
                                boolean takeOverChain,
                                String displayName) implements Interceptor {

    public ExtraStepExecutor(final Pointcut pointcut,
                             final TestCase beforeStep,
                             final TestCase afterStep,
                             final TestCase failureStep) {
        this(pointcut, beforeStep, afterStep, failureStep, true, null);
    }

    @Override
    public Stream<Interceptor> materialize(final InputData shareInput) {
        return Stream.of(new ExtraStepExecutor(this.pointcut.materialize(shareInput)
                , this.beforeStep.mapWhen(TestCase::isLazyLoad, it -> it.setShareInput(shareInput).build().execLazyLoad().builder())
                , this.afterStep.mapWhen(TestCase::isLazyLoad, it -> it.setShareInput(shareInput).build().execLazyLoad().builder())
                , this.failureStep.mapWhen(TestCase::isLazyLoad, it -> it.setShareInput(shareInput).build().execLazyLoad().builder())
                , this.takeOverChain
                , this.displayName
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
    public ExtraStepExecutor takeOverChain(final boolean newValue) {
        if (newValue == this.takeOverChain) {
            return this;
        }
        return new ExtraStepExecutor(this.pointcut, this.beforeStep, this.afterStep, this.failureStep, newValue, this.displayName);
    }

    public boolean hasDisplayName() {
        return !Optional.ofNullable(this.displayName()).orElse("").isEmpty();
    }

    public boolean hasStep() {
        return this.beforeStep().steps().size() > 0 || this.afterStep().steps().size() > 0 || this.failureStep().steps().size() > 0;
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

    public ExtraStepExecutor map(final UnaryOperator<ExtraStepExecutor.Builder> function) {
        return function.apply(this.builder()).build();
    }

    public Builder builder() {
        return new Builder()
                .setPointcut(this.pointcut)
                .addBefore(this.beforeStep)
                .addAfter(this.afterStep)
                .addFailure(this.failureStep)
                .setTakeOverChain(this.takeOverChain)
                .setDisplayName(this.displayName);
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

    public static class Builder {
        private Pointcut pointcut = Pointcut.NONE;
        private TestCase beforeStep = new TestCaseBuilder().build();
        private TestCase afterStep = new TestCaseBuilder().build();
        private TestCase failureStep = new TestCaseBuilder().build();
        private boolean takeOverChain = true;
        private String displayName;

        public Builder setPointcut(final Pointcut pointcut) {
            this.pointcut = pointcut;
            return this;
        }

        public Builder convertPointcut(final UnaryOperator<Pointcut> function) {
            return this.setPointcut(this.pointcut.convert(function));
        }

        public Builder addBefore(final TestCase testCase) {
            this.beforeStep = this.beforeStep.builder().addSteps(testCase.steps()).build();
            return this;
        }

        public Builder replaceBefore(final TestCase newValue) {
            this.beforeStep = newValue;
            return this;
        }

        public Builder addAfter(final TestCase testCase) {
            this.afterStep = this.afterStep.builder().addSteps(testCase.steps()).build();
            return this;
        }

        public Builder replaceAfter(final TestCase newValue) {
            this.afterStep = newValue;
            return this;
        }

        public Builder addFailure(final TestCase testCase) {
            this.failureStep = this.failureStep.builder().addSteps(testCase.steps()).build();
            return this;
        }

        public Builder replaceFailure(final TestCase newValue) {
            this.failureStep = newValue;
            return this;
        }

        public Builder setTakeOverChain(final boolean takeOverChain) {
            this.takeOverChain = takeOverChain;
            return this;
        }

        public Builder setDisplayName(final String displayName) {
            this.displayName = displayName;
            return this;
        }

        public ExtraStepExecutor build() {
            return new ExtraStepExecutor(this.pointcut
                    , this.beforeStep
                    , this.afterStep
                    , this.failureStep
                    , this.takeOverChain
                    , this.displayName);
        }

    }
}
