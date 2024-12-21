package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class ExportTemplate extends AbstractStepType implements LocatorHolder {

    @Override
    public boolean run(final TestRun ctx) {
        try {
            final ExportResource toExport = this.getExportResource(ctx);
            if (toExport.hasDataSource()) {
                toExport.outputDataSourceTemplate();
            }
            final String result = Context.toString(toExport.getScript());
            ctx.log().info(result);
            if (ctx.containsKey("file")) {
                final String fileName = ctx.string("file");
                final File outputTo = new File(ctx.getListener().getTemplateOutputDirectory(), fileName);
                return this.outputFile(ctx, result, outputTo);
            }
            return true;
        } catch (final IOException e) {
            ctx.log().error("output file failed cause:", e);
            return false;
        }
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("datasource")) {
            o.put("datasource", "");
        }
        if (!o.containsStringParam("file")) {
            o.put("file", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    private boolean outputFile(final TestRun ctx, final String result, final File outputTo) {
        try {
            if (outputTo.exists()) {
                Files.delete(outputTo.toPath());
            }
            Files.createFile(outputTo.toPath());
            Files.writeString(outputTo.toPath(), result, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            ctx.log().error("output file failed cause:", e);
            return false;
        }
        return true;
    }

    private ExportResource getExportResource(final TestRun ctx) {
        final boolean filterTag = ctx.getBoolean("filterTag");
        return new ExportResourceBuilder(ctx)
                .addInputStep(!filterTag || ctx.getBoolean("input"))
                .addSelectStep(!filterTag || ctx.getBoolean("select"))
                .addLinkClickStep(!filterTag || ctx.getBoolean("a"))
                .addButtonClickStep(!filterTag || ctx.getBoolean("button"))
                .addDivClickStep(filterTag && ctx.getBoolean("div"))
                .addSpanClickStep(filterTag && ctx.getBoolean("span"))
                .build();
    }

}
