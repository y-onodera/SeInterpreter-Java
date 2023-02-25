package com.sebuilder.interpreter.pointcut;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.step.type.SetElementText;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocatorFilterTest {

    private Step param = new StepBuilder(new SetElementText())
            .locator(new Locator("${type}", "${value}"))
            .build();

    @Test
    public void constructValueIsNotTargetType() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "id1"))
                .isHandle(null, this.param, new InputData().add("type", "name").add("value", "id1")));
    }

    @Test
    public void constructValueIsTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "id1"))
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueEqualTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "id1"), "equals")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueStartsWithTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "id"), "startsWith")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueEndsWithTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "1"), "endsWith")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueContainsTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "d"), "contains")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueMatchesTarget() {
        assertTrue(new LocatorFilter("locator", new Locator("id", "id\\d"), "matches")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueIsNotTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "id2"))
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotEqualTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "id2"), "equals")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotStartsWithTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "d"), "startsWith")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotEndsWithTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "d"), "endsWith")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotContainsTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "2"), "contains")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

    @Test
    public void constructValueNotMatchesTarget() {
        assertFalse(new LocatorFilter("locator", new Locator("id", "id\\d{2}"), "matches")
                .isHandle(null, this.param, new InputData().add("type", "id").add("value", "id1")));
    }

}