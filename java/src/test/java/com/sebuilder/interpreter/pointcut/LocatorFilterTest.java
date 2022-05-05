package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.*;

public class LocatorFilterTest {

    private Step param = new StepBuilder(new SetElementText())
            .locator(new Locator("${type}", "${value}"))
            .build();

    @Test
    public void constructValueIsNotTargetType() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "id1"))
                .test(param, new InputData().add("type", "name").add("value", "id1")));
    }

    @Test
    public void constructValueIsTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "id1"))
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueEqualTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "id1"), "equal")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueStartsWithTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "id"), "startsWith")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueEndsWithTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "1"), "endsWith")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueContainsTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "d"), "contains")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueMatchesTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "id\\d"), "matches")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueIsNotTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "id2"))
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotEqualTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "id2"), "equal")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotStartsWithTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "d"), "startsWith")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotEndsWithTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "d"), "endsWith")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotContainsTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "2"), "contains")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotMatchesTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "id\\d{2}"), "matches")
                .test(param, new InputData().add("type", "id").add("value", "id1")));
    }

}