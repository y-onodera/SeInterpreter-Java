package com.sebuilder.interpreter.script;

import com.sebuilder.interpreter.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.bidi.network.BytesValue;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class StepLoader {
    /**
     * @param o json object step load from
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    public ArrayList<Step> load(final JSONObject o) {
        final JSONArray stepsA = o.getJSONArray("steps");
        return IntStream.range(0, stepsA.length())
                .mapToObj(i -> this.load(stepsA, i))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected Step load(final JSONArray steps, final int i) {
        return this.createStep(steps.getJSONObject(i));
    }

    /**
     * @param stepO json object step load from
     * @return A new instance of step
     * @throws JSONException If anything goes wrong with interpreting the JSON.
     */
    protected Step createStep(final JSONObject stepO) {
        final StepType type = Context.getStepTypeOfName(stepO.getString("type"));
        final boolean isNegated = stepO.optBoolean("negated", false);
        final String name = stepO.optString("step_name", null);
        final StepBuilder step = new StepBuilder(name, type, isNegated);
        final JSONArray keysA = stepO.names();
        IntStream.range(0, keysA.length())
                .mapToObj(keysA::getString)
                .filter(key -> !key.equals("type") && !key.equals("negated"))
                .forEach(key -> {
                    if (stepO.optJSONArray(key) != null && key.equals("httpHeader")) {
                        JSONArray headers = stepO.getJSONArray(key);
                        IntStream.range(0, headers.length())
                                .mapToObj(headers::getJSONObject)
                                .forEach(obj -> step.put(obj.getString("key"), new BytesValueSource(BytesValue.Type.valueOf(obj.getString("type").toUpperCase())
                                        , obj.optString("value", "")
                                        , obj.optString("filePath", "")
                                        , obj.optBoolean("needEncoding", false)
                                )));
                    } else if (stepO.optJSONObject(key) != null && key.startsWith("locator")) {
                        step.put(key, new Locator(stepO.getJSONObject(key).getString("type"), stepO.getJSONObject(key).getString("value")));
                    } else {
                        step.put(key, stepO.getString(key));
                    }
                });
        return step.build();
    }

}
