package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class StringParamFilterTest {

    @Test
    public void constructValueIsTargetType() {
        assertTrue(new StringParamFilter("text", "test")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test")));
    }

    @Test
    public void constructValueEqualTargetType() {
        assertTrue(new StringParamFilter("text", "test", "equal")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test")));
    }

    @Test
    public void constructValueStartsWithTargetType() {
        assertTrue(new StringParamFilter("text", "test", "startsWith")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test1")));
    }

    @Test
    public void constructValueEndsWithTargetType() {
        assertTrue(new StringParamFilter("text", "test", "endsWith")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "some-test")));
    }

    @Test
    public void constructValueContainsTargetType() {
        assertTrue(new StringParamFilter("text", "test", "contains")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "this is test method")));
    }

    @Test
    public void constructValueMatchesTargetType() {
        assertTrue(new StringParamFilter("text", ".*test.*", "matches")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "this is test method")));
    }

    @Test
    public void constructValueIsNotTargetType() {
        assertFalse(new StringParamFilter("text", "test")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test1")));
    }

    @Test
    public void constructValueNotEqualTargetType() {
        assertFalse(new StringParamFilter("text", "test", "equal")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test1")));
    }

    @Test
    public void constructValueNotStartsWithTargetType() {
        assertFalse(new StringParamFilter("text", "test", "startsWith")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "this is test")));
    }

    @Test
    public void constructValueNotEndsWithTargetType() {
        assertFalse(new StringParamFilter("text", "test", "endsWith")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "test1")));
    }

    @Test
    public void constructValueNotContainsTargetType() {
        assertFalse(new StringParamFilter("text", "test", "contains")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "tes")));
    }

    @Test
    public void constructValueNotMatchesTargetType() {
        assertFalse(new StringParamFilter("text", ".*test.*", "matches")
                .test(new StepBuilder(new SetElementText()).put("text", "${text}").build(), new InputData().add("text", "this is a pen")));
    }

}