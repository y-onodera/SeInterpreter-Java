package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.Get;
import com.sebuilder.interpreter.step.type.SetElementSelected;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.*;

public class StepTypeFilterTest {

    @Test
    public void constructValueIsTargetType() {
        assertTrue(new StepTypeFilter("SetElementText")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueEqualTargetType() {
        assertTrue(new StepTypeFilter("SetElementText", "equal")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueStartsWithTargetType() {
        assertTrue(new StepTypeFilter("Set", "startsWith")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueEndsWithTargetType() {
        assertTrue(new StepTypeFilter("Text", "endsWith")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueContainsTargetType() {
        assertTrue(new StepTypeFilter("Element", "contains")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueMatchesTargetType() {
        assertTrue(new StepTypeFilter(".*Element.*", "matches")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void constructValueIsNotTargetType() {
        assertFalse(new StepTypeFilter("SetElementText")
                .test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void constructValueNotEqualTargetType() {
        assertFalse(new StepTypeFilter("SetElementText", "equal")
                .test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void constructValueNotStartsWithTargetType() {
        assertFalse(new StepTypeFilter("Set", "startsWith")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotEndsWithTargetType() {
        assertFalse(new StepTypeFilter("Text", "endsWith")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotContainsTargetType() {
        assertFalse(new StepTypeFilter("Element", "contains")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueNotMatchesTargetType() {
        assertFalse(new StepTypeFilter(".*Element.*", "matches")
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }


}