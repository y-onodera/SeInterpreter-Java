package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.devtools.NetworkInterceptor;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileDownloadCDP extends AbstractStepType implements ConditionalStep, LocatorHolder {

    @Override
    public boolean doRun(final TestRun ctx) {
        final NetworkInterceptor interceptor = new NetworkInterceptor(ctx.driver(), (HttpRequest req) -> {
            try {
                final HttpResponse res = HttpCommandExecutor.getDefaultClientFactory()
                        .createClient(new URL(req.getUri()))
                        .execute(req);
                this.downLoadFile(ctx, res);
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
            return new HttpResponse().setStatus(204);
        });
        final WebElement el = ctx.locator().find(ctx);
        el.click();
        interceptor.close();
        return true;
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("filepath")) {
            o.put("filepath", "");
        }
        if (!o.containsStringParam("fixedPath")) {
            o.put("fixedPath", "false");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    public void downLoadFile(final TestRun ctx, final HttpResponse response) throws IOException {
        final File outputFile;
        if (ctx.getBoolean("fixedPath")) {
            outputFile = ctx.getListener().addDownloadFile(ctx.string("filepath"));
        } else {
            outputFile = ctx.getListener().addDownloadFile(ctx.getTestRunName() + "_" + ctx.string("filepath"));
        }
        try (final InputStream inputStream = response.getContent().get(); final FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            int read;
            final byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
        }
    }

}
