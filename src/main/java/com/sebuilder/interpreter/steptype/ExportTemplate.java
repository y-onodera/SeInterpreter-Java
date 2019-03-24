package com.sebuilder.interpreter.steptype;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.StepType;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.export.ExportResource;
import com.sebuilder.interpreter.step.LocatorHolder;

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
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("datasource")) {
            o.put("datasource", "");
        }
        if (!o.containsStringParam("file")) {
            o.put("file", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
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

    private ExportResource getExportResource(TestRun ctx) {
        boolean filterTag = ctx.getBoolean("filterTag");
        return ExportResource.builder(ctx)
                .addInputStep(!filterTag || ctx.getBoolean("input"))
                .addSelectStep(!filterTag || ctx.getBoolean("select"))
                .addLinkClickStep(!filterTag || ctx.getBoolean("a"))
                .addButtonClickStep(!filterTag || ctx.getBoolean("button"))
                .addDivClickStep(filterTag && ctx.getBoolean("div"))
                .addSpanClickStep(filterTag && ctx.getBoolean("span"))
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        return this.getClass() == o.getClass();
    }

    @Override
    public int hashCode() {
        return this.getClass().getSimpleName().hashCode();
    }
}
