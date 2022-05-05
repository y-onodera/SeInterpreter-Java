package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.Get;
import org.junit.Test;

import static org.junit.Assert.*;

public class NegatedFilterTest {

    @Test
    public void constructValueFalseAndNegateFalseResultTrue() {
        assertTrue(new NegatedFilter(false)
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueFalseAndNegateTrueResultFalse() {
        assertFalse(new NegatedFilter(false)
                .test(new StepBuilder(new Get()).negated(true).build(), new InputData()));
    }

    @Test
    public void constructValueTrueAndNegateFalseResultFalse() {
        assertFalse(new NegatedFilter(true)
                .test(new StepBuilder(new Get()).build(), new InputData()));
    }

    @Test
    public void constructValueTrueAndNegateTrueResultTrue() {
        assertTrue(new NegatedFilter(true)
                .test(new StepBuilder(new Get()).negated(true).build(), new InputData()));
    }


}