package com.sebuilder.interpreter.steptype;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;

public class ExportTemplate implements StepType {

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    public boolean run(TestRun ctx) {
        try {
            JSONObject o = getJsonObject(ctx);
            String result = o.toString(4);
            ctx.log().info(result);
            if (ctx.containsKey("file")) {
                File outputTo = new File(Context.getInstance().getTemplateOutputDirectory(), ctx.string("file"));
                if (outputTo.exists()) {
                    outputTo.delete();
                }
                try {
                    outputTo.createNewFile();
                    Files.asCharSink(outputTo, Charsets.UTF_8).write(result);
                } catch (IOException e) {
                    ctx.log().error(e);
                    return false;
                }
            }
            return true;
        } catch (JSONException e) {
            ctx.log().error(e);
            return false;
        }
    }

    private JSONObject getJsonObject(TestRun ctx) throws JSONException {
        JSONObject o = new JSONObject();
        JSONArray stepsA = new JSONArray();
        addInputStep(ctx, stepsA);
        addSelectStep(ctx, stepsA);
        addLinkClickStep(ctx, stepsA);
        addButtonClickStep(ctx, stepsA);
        o.put("steps", stepsA);
        return o;
    }

    private void addLinkClickStep(TestRun ctx, JSONArray stepsA) {
        ctx.driver()
                .findElementsByTagName("a")
                .stream()
                .filter(element -> !Strings.isNullOrEmpty(element.getText()))
                .forEach(element -> {
                    try {
                        stepsA.put(new ClickElement().toJSON(ctx.driver(), element));
                    } catch (JSONException e) {
                        ctx.log().error(e);
                    }
                });
    }

    private void addButtonClickStep(TestRun ctx, JSONArray stepsA) {
        ctx.driver()
                .findElementsByTagName("button")
                .stream()
                .forEach(element -> {
                    try {
                        stepsA.put(new ClickElement().toJSON(ctx.driver(), element));
                    } catch (JSONException e) {
                        ctx.log().error(e);
                    }
                });
    }

    private void addSelectStep(TestRun ctx, JSONArray stepsA) {
        ctx.driver()
                .findElementsByTagName("select")
                .stream()
                .forEach(element -> {
                    element.findElements(By.tagName("option")).stream().forEach(option -> {
                        try {
                            stepsA.put(new SetElementSelected().toJSON(ctx.driver(), option));
                        } catch (JSONException e) {
                            ctx.log().error(e);
                        }
                    });
                });
    }

    private void addInputStep(TestRun ctx, JSONArray stepsA) {
        ctx.driver()
                .findElementsByTagName("input")
                .stream()
                .forEach(element -> {
                    try {
                        String type = element.getAttribute("type");
                        switch (type) {
                            case "text":
                                stepsA.put(new SetElementText().toJSON(ctx.driver(), element));
                                break;
                            case "checkbox":
                                stepsA.put(new SetElementSelected().toJSON(ctx.driver(), element));
                                break;
                            case "radio":
                                stepsA.put(new SetElementSelected().toJSON(ctx.driver(), element));
                                break;
                            case "hidden":
                                break;
                            default:
                                stepsA.put(new SetElementText().toJSON(ctx.driver(), element));
                        }
                    } catch (JSONException e) {
                        ctx.log().error(e);
                    }
                });
    }
}
