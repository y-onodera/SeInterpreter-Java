package com.sebuilder.interpreter;

import com.sebuilder.interpreter.step.type.SetElementSelected;
import org.junit.Test;

import static org.junit.Assert.*;

public class PointcutTest {

    @Test
    public void testNoneReturnFalse(){
        assertFalse(Pointcut.NONE.test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void testAnyReturnTrue(){
        assertTrue(Pointcut.ANY.test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void testOrCombination(){
        assertTrue(Pointcut.ANY.or(Pointcut.ANY).test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertTrue(Pointcut.ANY.or(Pointcut.NONE).test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertTrue(Pointcut.NONE.or(Pointcut.ANY).test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertFalse(Pointcut.NONE.or(Pointcut.NONE).test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void testAndCombination(){
        assertTrue(Pointcut.ANY.and(Pointcut.ANY).test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertFalse(Pointcut.ANY.and(Pointcut.NONE).test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertFalse(Pointcut.NONE.and(Pointcut.ANY).test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertFalse(Pointcut.NONE.and(Pointcut.NONE).test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }
}