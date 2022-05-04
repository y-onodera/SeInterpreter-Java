package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.SetElementSelected;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.*;

public class StepTypeFilterTest {

    @Test
    public void valueConstructTestTargetType() {
        assertTrue(new StepTypeFilter("SetElementText")
                .test(new StepBuilder(new SetElementText()).build(), new InputData()));
    }

    @Test
    public void valueConstructTestNotTargetType() {
        assertFalse(new StepTypeFilter("SetElementText")
                .test(new StepBuilder(new SetElementSelected()).build(), new InputData()));
    }

}