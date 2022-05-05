package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.Get;
import com.sebuilder.interpreter.step.type.SelectElementValue;
import com.sebuilder.interpreter.step.type.SetElementSelected;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.*;

public class StepTypeFilterTest {

    @Test
    public void constructValueIsTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("SetElementText")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueEqualTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("SetElementText", "equal")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueStartsWithTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("Set", "startsWith")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueEndsWithTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("Text", "endsWith")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueContainsTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("Element", "contains")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueMatchesTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter(".*Element.*", "matches")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueEqualTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("SetElementText", "!equal")
                .test(new StepBuilder(new SelectElementValue()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueStartsWithTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("Set", "!startsWith")
                .test(new StepBuilder(new SelectElementValue()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueEndsWithTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("Text", "!endsWith")
                .test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueNotContainsTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter("Element", "!contains")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void negateConstructValueNotMatchesTargetTypeResultTrue() {
        assertTrue(new StepTypeFilter(".*Element.*", "!matches")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueIsNotTargetTypeResultFalse() {
        assertFalse(new StepTypeFilter("SetElementText")
                .test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void constructValueNotEqualTargetTypeResultFalse() {
        assertFalse(new StepTypeFilter("SetElementText", "equal")
                .test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void constructValueNotStartsWithTargetTypeResultFalse() {
        assertFalse(new StepTypeFilter("Set", "startsWith")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotEndsWithTargetTypeResultFalse() {
        assertFalse(new StepTypeFilter("Text", "endsWith")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotContainsTargetTypeResultFalse() {
        assertFalse(new StepTypeFilter("Element", "contains")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotMatchesTargetTypeResultFalse() {
        assertFalse(new StepTypeFilter(".*Element.*", "matches")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }


}