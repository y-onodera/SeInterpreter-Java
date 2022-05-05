package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.*;

public class SkipFilterTest {

    @Test
    public void constructValueTrueAndTargetSkipTrueResultTrue() {
        assertTrue(new SkipFilter(true)
                .test(new StepBuilder(new SetElementText()).put("skip", "${test}").build(), new InputData().add("test", "true")));
    }

    @Test
    public void constructValueFalseAndTargetSkipTrueResultFalse() {
        assertFalse(new SkipFilter(false)
                .test(new StepBuilder(new SetElementText()).put("skip", "${test}").build(), new InputData().add("test", "true")));
    }

    @Test
    public void constructValueFalseAndTargetSkipFalseResultTrue() {
        assertTrue(new SkipFilter(false)
                .test(new StepBuilder(new SetElementText()).put("skip", "${test}").build(), new InputData().add("test", "false")));
    }

    @Test
    public void constructValueTrueAndTargetSkipFalseResultFalse() {
        assertFalse(new SkipFilter(true)
                .test(new StepBuilder(new SetElementText()).put("skip", "${test}").build(), new InputData().add("test", "false")));
    }


}