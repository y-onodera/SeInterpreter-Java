package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.*;
import com.sebuilder.interpreter.datasource.DataSourceFactoryImpl;
import com.sebuilder.interpreter.step.StepTypeFactoryImpl;
import com.sebuilder.interpreter.step.type.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

import java.io.File;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

@RunWith(Enclosed.class)
public class SebuilderLoadAspectTest {

    public static abstract class SingleFilter {

        static {
            Context.getInstance()
                    .setStepTypeFactory(new StepTypeFactoryImpl())
                    .setDataSourceFactory(new DataSourceFactoryImpl());
        }

        protected Sebuilder target = new Sebuilder();

        protected File scriptDir = new File(Objects.requireNonNull(SebuilderLoadAspectTest.class.getResource(".")).getPath(), "aspect");

        protected Aspect result;

        @Before
        public void setup() {
            this.result = this.target.loadAspect(new File(this.scriptDir, "aspectWithSingleFilterPointcut.json"));
        }

        protected void assertNotWeaver(final Step step, final InputData inputData) {
            final var advice = this.result.advice(step, inputData)
                    .advices();
            assertEquals(0, advice.size());
        }

        protected void assertWeaver(final Step step, final InputData inputData) {
            final var advice = this.result.advice(step, inputData)
                    .advices();
            assertEquals(1, advice.size());
            assertEquals(1, getInterceptor(advice, 0).beforeStep().steps().size());
            assertEquals(new Get().toStep().build(), getInterceptor(advice, 0).beforeStep().steps().get(0));
            assertEquals(new TestCaseBuilder().build(), getInterceptor(advice, 0).afterStep());
            assertEquals(new TestCaseBuilder().build(), getInterceptor(advice, 0).failureStep());
        }
    }

    public static class StringParamPointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            this.assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "text").build(), new InputData());
        }

        @Test
        public void adviceWeaverPointcutValues() {
            this.assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "id1").build(), new InputData());
            this.assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "id2").build(), new InputData());
            this.assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "id3").build(), new InputData());
        }

        @Test
        public void adviceWeaverStepEqualsPointcutMethod() {
            this.assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "agent007").build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            this.assertNotWeaver(new StepBuilder(new SendKeysToElement()).put("text", "007").build(), new InputData());
        }

    }

    public static class TypePointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            this.assertWeaver(new StepBuilder(new ClickElement()).build(), new InputData());
        }

        @Test
        public void adviceWeaverPointcutValues() {
            this.assertWeaver(new StepBuilder(new SetElementSelected()).build(), new InputData());
            this.assertWeaver(new StepBuilder(new SetElementText()).build(), new InputData());
            this.assertWeaver(new StepBuilder(new SelectElementValue()).build(), new InputData());
        }

        @Test
        public void adviceWeaverStepEqualsPointcutMethod() {
            this.assertWeaver(new StepBuilder(new ScrollDown()).build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            this.assertNotWeaver(new StepBuilder(new Get()).build(), new InputData());
        }

    }

    public static class NegatePointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            this.assertWeaver(new StepBuilder(new Get()).negated(true).build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            this.assertNotWeaver(new StepBuilder(new Get()).build(), new InputData());
        }

    }

    public static class LocatorPointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            this.assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("name", "name1"))
                    .build(), new InputData());
        }

        @Test
        public void adviceWeaverPointcutValues() {
            this.assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("id", "id1"))
                    .build(), new InputData());
            this.assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("id", "id2"))
                    .build(), new InputData());
            this.assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("id", "id3"))
                    .build(), new InputData());
        }

        @Test
        public void adviceWeaverStepEqualsPointcutMethod() {
            this.assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("link text", "some link text"))
                    .build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            this.assertNotWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("css selector", "some link text"))
                    .build(), new InputData());
        }

    }


    public static class SkipPointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            this.assertWeaver(new StepBuilder(new Get()).skip("true").build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            this.assertNotWeaver(new StepBuilder(new Get()).build(), new InputData());
        }

    }

    public static class MultiAdviceTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            final var advice = this.result.advice(new ClickElement().toStep()
                            .negated(true)
                            .skip("true")
                            .locator(new Locator("id", "id1"))
                            .put("text", "text")
                            .build(), new InputData())
                    .advices();
            assertEquals(5, advice.size());
            assertEquals(1, getInterceptor(advice, 0).beforeStep().steps().size());
            assertEquals(0, getInterceptor(advice, 0).afterStep().steps().size());
            assertEquals(0, getInterceptor(advice, 0).failureStep().steps().size());
            assertEquals(new Get().toStep().build(), getInterceptor(advice, 0).beforeStep().steps().get(0));
            assertEquals(1, getInterceptor(advice, 1).beforeStep().steps().size());
            assertEquals(0, getInterceptor(advice, 1).afterStep().steps().size());
            assertEquals(0, getInterceptor(advice, 1).failureStep().steps().size());
            assertEquals(new Get().toStep().build(), getInterceptor(advice, 1).beforeStep().steps().get(0));
            assertEquals(1, getInterceptor(advice, 2).beforeStep().steps().size());
            assertEquals(0, getInterceptor(advice, 2).afterStep().steps().size());
            assertEquals(0, getInterceptor(advice, 2).failureStep().steps().size());
            assertEquals(new Get().toStep().build(), getInterceptor(advice, 2).beforeStep().steps().get(0));
            assertEquals(1, getInterceptor(advice, 3).beforeStep().steps().size());
            assertEquals(0, getInterceptor(advice, 3).afterStep().steps().size());
            assertEquals(0, getInterceptor(advice, 3).failureStep().steps().size());
            assertEquals(new Get().toStep().build(), getInterceptor(advice, 3).beforeStep().steps().get(0));
            assertEquals(1, getInterceptor(advice, 4).beforeStep().steps().size());
            assertEquals(0, getInterceptor(advice, 4).afterStep().steps().size());
            assertEquals(0, getInterceptor(advice, 4).failureStep().steps().size());
            assertEquals(new Get().toStep().build(), getInterceptor(advice, 4).beforeStep().steps().get(0));
        }

    }

    public static class MultiFilterTest extends SingleFilter {

        @Before
        @Override
        public void setup() {
            this.result = this.target.loadAspect(new File(this.scriptDir, "aspectWithMultiFilterPointcut.json"));
        }

        @Test
        public void adviceWeaverPointcutValue() {
            this.assertWeaver(new SetElementText().toStep().locator(new Locator("id", "id1")).build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            this.assertNotWeaver(new SetElementText().toStep().locator(new Locator("id", "id1"))
                    .skip("true").build(), new InputData());
            this.assertNotWeaver(new SetElementText().toStep().locator(new Locator("id", "id4"))
                    .build(), new InputData());
            this.assertNotWeaver(new ClickElement().toStep().locator(new Locator("id", "id1"))
                    .build(), new InputData());
        }

        @Override
        protected void assertWeaver(final Step step, final InputData inputData) {
            final var advice = this.result.advice(step, inputData)
                    .advices();
            assertEquals(1, advice.size());
            assertEquals(2, getInterceptor(advice, 0).beforeStep().steps().size());
            assertEquals(new Get().toStep().build(), getInterceptor(advice, 0).beforeStep().steps().get(0));
            assertEquals(new SetElementText().toStep().put("text", "before step").build(), getInterceptor(advice, 0).beforeStep().steps().get(1));
            assertEquals(2, getInterceptor(advice, 0).afterStep().steps().size());
            assertEquals(new SetElementText().toStep().put("text", "after step").build(), getInterceptor(advice, 0).afterStep().steps().get(0));
            assertEquals(new Get().toStep().build(), getInterceptor(advice, 0).afterStep().steps().get(1));
            assertEquals(2, getInterceptor(advice, 0).failureStep().steps().size());
            assertEquals(new SetElementSelected().toStep().build(), getInterceptor(advice, 0).failureStep().steps().get(0));
            assertEquals(new SetElementText().toStep().put("text", "failure step").build(), getInterceptor(advice, 0).failureStep().steps().get(1));
        }
    }

    private static ExtraStepExecutor getInterceptor(final List<Interceptor> advice, final int index) {
        return (ExtraStepExecutor) advice.get(index);
    }

}
