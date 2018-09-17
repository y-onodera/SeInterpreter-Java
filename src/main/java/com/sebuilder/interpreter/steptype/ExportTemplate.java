package com.sebuilder.interpreter.steptype;

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.ExportResource;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

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
            ExportResource toExport = getExportResource(ctx);
            if (toExport.hasDataSource()) {
                toExport.outputDataSourceTemplate();
            }
            String result = toExport.getScript();
            ctx.log().info(result);
            if (ctx.containsKey("file")) {
                String fileName = ctx.string("file");
                File outputTo = new File(Context.getInstance().getTemplateOutputDirectory(), fileName);
                return outputFile(ctx, result, outputTo, Charsets.UTF_8);
            }
            return true;
        } catch (JSONException | IOException  e) {
            ctx.log().error(e);
            return false;
        }
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        if (!o.has("datasource")) {
            o.put("datasource", "false");
        }
        if (!o.has("file")) {
            o.put("file", "");
        }
    }

    private boolean outputFile(TestRun ctx, String result, File outputTo, Charset charset) {
        if (outputTo.exists()) {
            outputTo.delete();
        }
        try {
            outputTo.createNewFile();
            Files.asCharSink(outputTo, charset).write(result);
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    private ExportResource getExportResource(TestRun ctx) throws JSONException {
        ExportResource.Builder builder = ExportResource.builder(ctx);
        addInputStep(ctx, builder);
        addSelectStep(ctx, builder);
        addLinkClickStep(ctx, builder);
        addButtonClickStep(ctx, builder);
        return builder.build();
    }

    private void addLinkClickStep(TestRun ctx, ExportResource.Builder builder) {
        ctx.driver()
                .findElementsByTagName("a")
                .stream()
                .filter(element -> !Strings.isNullOrEmpty(element.getText()))
                .forEach(element -> {
                    builder.addStep(new ClickElement(), ctx.driver(), element);
                });
    }

    private void addButtonClickStep(TestRun ctx, ExportResource.Builder builder) {
        ctx.driver()
                .findElementsByTagName("button")
                .stream()
                .forEach(element -> {
                    builder.addStep(new ClickElement(), ctx.driver(), element);
                });
    }

    private void addSelectStep(TestRun ctx, ExportResource.Builder builder) {
        ctx.driver()
                .findElementsByTagName("select")
                .stream()
                .forEach(element -> {
                    element.findElements(By.tagName("option")).stream().forEach(option -> {
                        builder.addStep(new SetElementSelected(), ctx.driver(), option);
                    });
                });
    }

    private void addInputStep(TestRun ctx, ExportResource.Builder builder) {
        ctx.driver()
                .findElementsByTagName("input")
                .stream()
                .forEach(element -> {
                    String type = element.getAttribute("type");
                    switch (type) {
                        case "text":
                            builder.addStep(new SetElementText(), ctx.driver(), element);
                            break;
                        case "checkbox":
                        case "radio":
                            builder.addStep(new SetElementSelected(), ctx.driver(), element);
                            break;
                        case "hidden":
                            break;
                        default:
                            builder.addStep(new SetElementText(), ctx.driver(), element);
                    }
                });
    }

}
