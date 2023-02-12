package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.InputData;
import com.sebuilder.interpreter.Locator;
import com.sebuilder.interpreter.Step;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.step.Loop;
import com.sebuilder.interpreter.step.getter.ElementAttribute;
import com.sebuilder.interpreter.step.type.ClickElement;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;

public class StepLoaderTest {
    private static final String baseDir = Objects.requireNonNull(StepLoaderTest.class.getResource(".")).getPath();
    private static final File testFileScriptWithSteps = new File(baseDir, "scriptWithSteps.json");

    @Test
    public void parseStep() throws FileNotFoundException {
        final List<Step> results = new StepLoader().load(new JSONObject(new JSONTokener(new FileReader(testFileScriptWithSteps))));
        assertEquals(10, results.size());
        this.assertStep(results, 0, new ClickElement(), Locator.Type.ID, "id1", false, false);

        this.assertStep(results, 1, new ClickElement(), Locator.Type.NAME, "name1", true, false);
        this.assertStep(results, 2, new ElementAttribute().toWaitFor(), Locator.Type.LINK_TEXT, "link", false, false);
        this.assertStep(results, 3, new ElementAttribute().toVerify(), Locator.Type.CSS_SELECTOR, "selector", false, false);
        this.assertStep(results, 4, new ElementAttribute().toAssert(), Locator.Type.XPATH, "//", false, false);
        this.assertStep(results, 5, new ElementAttribute().toStore(), Locator.Type.CSS_SELECTOR, "selector", false, true);
        this.assertStep(results, 6, new ElementAttribute().toPrint(), Locator.Type.XPATH, "//", false, false);
        this.assertStep(results, 7, new ElementAttribute().toIf(), Locator.Type.ID, "id1", false, false);
        this.assertStep(results, 8, new ElementAttribute().toRetry(), Locator.Type.NAME, "name1", false, false);
        assertEquals(results.get(9).type(), new Loop());
    }

    private void assertStep(final List<Step> results, final int i, final StepType stepType, final Locator.Type locatorName, final String locatorValue, final boolean isSkip, final boolean isNageted) {
        assertEquals(results.get(i).type(), stepType);
        assertEquals(results.get(i).getLocator("locator").type(), locatorName.toString());
        assertEquals(results.get(i).getLocator("locator").value(), locatorValue);
        assertEquals(results.get(i).isSkip(new InputData()), isSkip);
        assertEquals(results.get(i).negated(), isNageted);
    }
}