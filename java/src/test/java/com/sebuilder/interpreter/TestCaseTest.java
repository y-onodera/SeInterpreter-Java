package com.sebuilder.interpreter;

import com.sebuilder.interpreter.step.type.ClickElement;
import com.sebuilder.interpreter.step.type.DoubleClickElement;
import com.sebuilder.interpreter.step.type.SendKeysToElement;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class TestCaseTest {

    public static class BasicCaseTest {

        private TestCase target;

        @Before
        public void setup() {
            this.target = new TestCaseBuilder()
                    .addStep(new Step(new DoubleClickElement()))
                    .addStep(new Step(new ClickElement()))
                    .addStep(new Step(new SetElementText()))
                    .build();
        }

        @Test
        public void removeStep() {
            TestCase result = this.target.removeStep(2);
            Assert.assertEquals(2, result.steps().size());
            Assert.assertEquals(new DoubleClickElement(), result.steps().get(0).type());
            Assert.assertEquals(new ClickElement(), result.steps().get(1).type());
        }

        @Test
        public void filterStep() {
            TestCase result = this.target.filterStep(it -> it.intValue() == 2);
            Assert.assertEquals(1, result.steps().size());
            Assert.assertEquals(new SetElementText(), result.steps().get(0).type());
        }

        @Test
        public void insertStep() {
            TestCase result = this.target.insertStep(1, new SendKeysToElement().toStep().build());
            Assert.assertEquals(4, result.steps().size());
            Assert.assertEquals(new DoubleClickElement(), result.steps().get(0).type());
            Assert.assertEquals(new SendKeysToElement(), result.steps().get(1).type());
            Assert.assertEquals(new ClickElement(), result.steps().get(2).type());
            Assert.assertEquals(new SetElementText(), result.steps().get(3).type());
        }

        @Test
        public void addStep() {
            TestCase result = this.target.addStep(1, new SendKeysToElement().toStep().build());
            Assert.assertEquals(4, result.steps().size());
            Assert.assertEquals(new DoubleClickElement(), result.steps().get(0).type());
            Assert.assertEquals(new ClickElement(), result.steps().get(1).type());
            Assert.assertEquals(new SendKeysToElement(), result.steps().get(2).type());
            Assert.assertEquals(new SetElementText(), result.steps().get(3).type());
        }

        @Test
        public void replaceSteps() {
            TestCase result = this.target.replaceSteps(1, new SendKeysToElement().toStep().build());
            Assert.assertEquals(3, result.steps().size());
            Assert.assertEquals(new DoubleClickElement(), result.steps().get(0).type());
            Assert.assertEquals(new SendKeysToElement(), result.steps().get(1).type());
            Assert.assertEquals(new SetElementText(), result.steps().get(2).type());
        }
    }

    public static class EmptyStepsCaseTest {

        private TestCase target;

        @Before
        public void setup() {
            this.target = new TestCaseBuilder().build();
        }

        @Test
        public void removeStep() {
            TestCase result = this.target.removeStep(2);
            Assert.assertEquals(0, result.steps().size());
        }

        @Test
        public void filterStep() {
            TestCase result = this.target.filterStep(it -> it.intValue() == 2);
            Assert.assertEquals(0, result.steps().size());
        }

        @Test
        public void insertStep() {
            TestCase result = this.target.insertStep(1, new SendKeysToElement().toStep().build());
            Assert.assertEquals(1, result.steps().size());
            Assert.assertEquals(new SendKeysToElement(), result.steps().get(0).type());
        }

        @Test
        public void addStep() {
            TestCase result = this.target.addStep(1, new SendKeysToElement().toStep().build());
            Assert.assertEquals(1, result.steps().size());
            Assert.assertEquals(new SendKeysToElement(), result.steps().get(0).type());
        }

        @Test
        public void replaceSteps() {
            TestCase result = this.target.replaceSteps(1, new SendKeysToElement().toStep().build());
            Assert.assertEquals(0, result.steps().size());
        }
    }
}