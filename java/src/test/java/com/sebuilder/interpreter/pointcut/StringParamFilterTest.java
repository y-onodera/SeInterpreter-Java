package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringParamFilterTest {
    @Test
    public void constructValueIsTarSetElementTextType() {
        assertTrue(new StringParamFilter("text", "test")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test")));
    }

    @Test
    public void constructValueEqualTarSetElementTextType() {
        assertTrue(new StringParamFilter("text", "test", "equal")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test")));
    }

    @Test
    public void constructValueStartsWithTarSetElementTextType() {
        assertTrue(new StringParamFilter("text", "test", "startsWith")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test1")));
    }

    @Test
    public void constructValueEndsWithTarSetElementTextType() {
        assertTrue(new StringParamFilter("text", "test", "endsWith")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "some-test")));
    }

    @Test
    public void constructValueContainsTarSetElementTextType() {
        assertTrue(new StringParamFilter("text", "test", "contains")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "this is test method")));
    }

    @Test
    public void constructValueMatchesTarSetElementTextType() {
        assertTrue(new StringParamFilter("text", ".*test.*", "matches")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "this is test method")));
    }

    @Test
    public void constructValueIsNotTarSetElementTextType() {
        assertFalse(new StringParamFilter("text", "test")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test1")));
    }

    @Test
    public void constructValueNotEqualTarSetElementTextType() {
        assertFalse(new StringParamFilter("text", "test", "equal")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test1")));
    }

    @Test
    public void constructValueNotStartsWithTarSetElementTextType() {
        assertFalse(new StringParamFilter("text", "test", "startsWith")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "this is test")));
    }

    @Test
    public void constructValueNotEndsWithTarSetElementTextType() {
        assertFalse(new StringParamFilter("text", "test", "endsWith")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test1")));
    }

    @Test
    public void constructValueNotContainsTarSetElementTextType() {
        assertFalse(new StringParamFilter("text", "test", "contains")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "tes")));
    }

    @Test
    public void constructValueNotMatchesTarSetElementTextType() {
        assertFalse(new StringParamFilter("text", ".*test.*", "matches")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "this is a pen")));
    }

}