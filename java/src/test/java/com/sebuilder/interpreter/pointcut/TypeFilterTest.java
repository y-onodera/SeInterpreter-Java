package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.Get;
import com.sebuilder.interpreter.step.type.SelectElementValue;
import com.sebuilder.interpreter.step.type.SetElementSelected;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TypeFilterTest {

    @Test
    public void constructValueIsTargetTypeResultTrue() {
        assertTrue(new TypeFilter("SetElementText")
                .isHandle(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueEqualTargetTypeResultTrue() {
        assertTrue(new TypeFilter("SetElementText", "equals")
                .isHandle(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueStartsWithTargetTypeResultTrue() {
        assertTrue(new TypeFilter("Set", "startsWith")
                .isHandle(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueEndsWithTargetTypeResultTrue() {
        assertTrue(new TypeFilter("Text", "endsWith")
                .isHandle(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueContainsTargetTypeResultTrue() {
        assertTrue(new TypeFilter("Element", "contains")
                .isHandle(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueMatchesTargetTypeResultTrue() {
        assertTrue(new TypeFilter(".*Element.*", "matches")
                .isHandle(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueEqualTargetTypeResultTrue() {
        assertTrue(new TypeFilter("SetElementText", "!equals")
                .isHandle(new StepBuilder(new SelectElementValue()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueStartsWithTargetTypeResultTrue() {
        assertTrue(new TypeFilter("Set", "!startsWith")
                .isHandle(new StepBuilder(new SelectElementValue()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueEndsWithTargetTypeResultTrue() {
        assertTrue(new TypeFilter("Text", "!endsWith")
                .isHandle(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueNotContainsTargetTypeResultTrue() {
        assertTrue(new TypeFilter("Element", "!contains")
                .isHandle(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueNotMatchesTargetTypeResultTrue() {
        assertTrue(new TypeFilter(".*Element.*", "!matches")
                .isHandle(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueIsNotTargetTypeResultFalse() {
        assertFalse(new TypeFilter("SetElementText")
                .isHandle(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void constructValueNotEqualTargetTypeResultFalse() {
        assertFalse(new TypeFilter("SetElementText", "equals")
                .isHandle(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void constructValueNotStartsWithTargetTypeResultFalse() {
        assertFalse(new TypeFilter("Set", "startsWith")
                .isHandle(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotEndsWithTargetTypeResultFalse() {
        assertFalse(new TypeFilter("Text", "endsWith")
                .isHandle(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotContainsTargetTypeResultFalse() {
        assertFalse(new TypeFilter("Element", "contains")
                .isHandle(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotMatchesTargetTypeResultFalse() {
        assertFalse(new TypeFilter(".*Element.*", "matches")
                .isHandle(new StepBuilder(new Get()).build(), new InputData()));
    }


}