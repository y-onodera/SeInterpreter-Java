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
import java.io.IOException;
import java.util.ArrayList;

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

        protected File scriptDir = new File(SebuilderLoadAspectTest.class.getResource(".").getPath(), "aspect");

        protected Aspect result;

        @Before
        public void setup() throws IOException {
            result = target.loadAspect(new File(scriptDir, "aspectWithSingleFilterPointcut.json"));
        }

        protected void assertNotWeaver(Step step, InputData inputData) {
            var advice = result.advice(step, inputData)
                    .advices();
            assertEquals(0, advice.size());
        }

        protected void assertWeaver(Step step, InputData inputData) {
            var advice = result.advice(step, inputData)
                    .advices();
            assertEquals(1, advice.size());
            assertEquals(1, advice.get(0).beforeStep().size());
            assertEquals(new Get().toStep().build(), advice.get(0).beforeStep().get(0));
            assertEquals(new ArrayList<>(), advice.get(0).afterStep());
            assertEquals(new ArrayList<>(), advice.get(0).failureStep());
        }
    }

    public static class StringParamPointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "text").build(), new InputData());
        }

        @Test
        public void adviceWeaverPointcutValues() {
            assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "id1").build(), new InputData());
            assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "id2").build(), new InputData());
            assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "id3").build(), new InputData());
        }

        @Test
        public void adviceWeaverStepEqualsPointcutMethod() {
            assertWeaver(new StepBuilder(new SendKeysToElement()).put("text", "agent007").build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            assertNotWeaver(new StepBuilder(new SendKeysToElement()).put("text", "007").build(), new InputData());
        }

    }

    public static class TypePointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            assertWeaver(new StepBuilder(new ClickElement()).build(), new InputData());
        }

        @Test
        public void adviceWeaverPointcutValues() {
            assertWeaver(new StepBuilder(new SetElementSelected()).build(), new InputData());
            assertWeaver(new StepBuilder(new SetElementText()).build(), new InputData());
            assertWeaver(new StepBuilder(new SelectElementValue()).build(), new InputData());
        }

        @Test
        public void adviceWeaverStepEqualsPointcutMethod() {
            assertWeaver(new StepBuilder(new ScrollDown()).build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            assertNotWeaver(new StepBuilder(new Get()).build(), new InputData());
        }

    }

    public static class NegatePointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            assertWeaver(new StepBuilder(new Get()).negated(true).build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            assertNotWeaver(new StepBuilder(new Get()).build(), new InputData());
        }

    }

    public static class LocatorPointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("name", "name1"))
                    .build(), new InputData());
        }

        @Test
        public void adviceWeaverPointcutValues() {
            assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("id", "id1"))
                    .build(), new InputData());
            assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("id", "id2"))
                    .build(), new InputData());
            assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("id", "id3"))
                    .build(), new InputData());
        }

        @Test
        public void adviceWeaverStepEqualsPointcutMethod() {
            assertWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("link text", "some link text"))
                    .build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            assertNotWeaver(new StepBuilder(new DoubleClickElement())
                    .locator(new Locator("css selector", "some link text"))
                    .build(), new InputData());
        }

    }


    public static class SkipPointcutTest extends SingleFilter {

        @Test
        public void adviceWeaverPointcutValue() {
            assertWeaver(new StepBuilder(new Get()).skip("true").build(), new InputData());
        }

        @Test
        public void adviceNotWeaverStepIsNotPointcutTarget() {
            assertNotWeaver(new StepBuilder(new Get()).build(), new InputData());
        }

    }
}
