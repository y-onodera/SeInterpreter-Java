package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.Get;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NegatedFilterTest {

    @Test
    public void constructValueFalseAndNegateFalseResultTrue() {
        assertTrue(new NegatedFilter(false)
                .isHandle(null, new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueFalseAndNegateTrueResultFalse() {
        assertFalse(new NegatedFilter(false)
                .isHandle(null, new StepBuilder(new Get()).negated(true).build(), new InputData()));
    }

    @Test
    public void constructValueTrueAndNegateFalseResultFalse() {
        assertFalse(new NegatedFilter(true)
                .isHandle(null, new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueTrueAndNegateTrueResultTrue() {
        assertTrue(new NegatedFilter(true)
                .isHandle(null, new StepBuilder(new Get()).negated(true).build(), new InputData()));
    }


}