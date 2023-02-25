package com.sebuilder.interpreter;

import com.sebuilder.interpreter.step.type.SetElementSelected;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PointcutTest {

    @Test
    public void testNoneReturnFalse() {
        assertFalse(Pointcut.NONE.isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void testAnyReturnTrue() {
        assertTrue(Pointcut.ANY.isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void testOrCombination() {
        assertTrue(Pointcut.ANY.or(Pointcut.ANY).isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertTrue(Pointcut.ANY.or(Pointcut.NONE).isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertTrue(Pointcut.NONE.or(Pointcut.ANY).isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertFalse(Pointcut.NONE.or(Pointcut.NONE).isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

    @Test
    public void testAndCombination() {
        assertTrue(Pointcut.ANY.and(Pointcut.ANY).isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertFalse(Pointcut.ANY.and(Pointcut.NONE).isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertFalse(Pointcut.NONE.and(Pointcut.ANY).isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
        assertFalse(Pointcut.NONE.and(Pointcut.NONE).isHandle(null, new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }
}