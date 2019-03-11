package com.sebuilder.interpreter.factory;

import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestData;
import com.sebuilder.interpreter.steptype.ClickElement;
import com.sebuilder.interpreter.steptype.ElementAttribute;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

public class StepTypeFactoryTest {

    private String baseDir = this.getClass().getResource(".").getPath();
    private StepTypeFactory target = new StepTypeFactory();

    @Test
    public void parseStep() throws FileNotFoundException, JSONException {
        File file = new File(this.baseDir, "scriptWithSteps.json");
        List<Step> results = target.parseStep(new JSONObject(new JSONTokener(new FileReader(file))));
        assertEquals(9, results.size());
        assertStep(results, 0, new ClickElement(), Locator.Type.ID, "id1", false, false);
        assertStep(results, 1, new ClickElement(), Locator.Type.NAME, "name1", true, false);
        assertStep(results, 2, new ElementAttribute().toWaitFor(), Locator.Type.LINK_TEXT, "link", false, false);
        assertStep(results, 3, new ElementAttribute().toVerify(), Locator.Type.CSS_SELECTOR, "selector", false, false);
        assertStep(results, 4, new ElementAttribute().toAssert(), Locator.Type.XPATH, "//", false, false);
        assertStep(results, 5, new ElementAttribute().toStore(), Locator.Type.CSS_SELECTOR, "selector", false, true);
        assertStep(results, 6, new ElementAttribute().toPrint(), Locator.Type.XPATH, "//", false, false);
        assertStep(results, 7, new ElementAttribute().toIf(), Locator.Type.ID, "id1", false, false);
        assertStep(results, 8, new ElementAttribute().toRetry(), Locator.Type.NAME, "name1", false, false);
    }

    private void assertStep(List<Step> results, int i, StepType stepType, Locator.Type locatorName, String locatorValue, boolean isSkip, boolean isNageted) {
        assertEquals(results.get(i).getType(), stepType);
        assertSame(results.get(i).getLocator("locator").type, locatorName);
        assertEquals(results.get(i).getLocator("locator").value, locatorValue);
        assertEquals(results.get(i).isSkip(new TestData()), isSkip);
        assertEquals(results.get(i).isNegated(), isNageted);
    }
}