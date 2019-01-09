package com.sebuilder.interpreter.steptype;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sebuilder.interpreter.ExportResource;
import com.sebuilder.interpreter.LocatorHolder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class ExportTemplate implements StepType, LocatorHolder {

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
                File outputTo = new File(ctx.getListener().getTemplateOutputDirectory(), fileName);
                return outputFile(ctx, result, outputTo, Charsets.UTF_8);
            }
            return true;
        } catch (JSONException | IOException e) {
            ctx.log().error(e);
            return false;
        }
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        LocatorHolder.super.supplementSerialized(o);
        if (!o.has("datasource")) {
            o.put("datasource", "");
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
        return ExportResource.builder(ctx)
                .addInputStep()
                .addSelectStep()
                .addLinkClickStep()
                .addButtonClickStep()
                .build();
    }
}
