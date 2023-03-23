package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.http.HttpClient;
import org.openqa.selenium.remote.http.HttpMethod;
import org.openqa.selenium.remote.http.HttpRequest;
import org.openqa.selenium.remote.http.HttpResponse;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

public class FileDownload extends AbstractStepType implements ConditionalStep, LocatorHolder {

    @Override
    public boolean doRun(final TestRun ctx) {
        if (ctx.containsKey("postURL")) {
            return this.post(ctx);
        }
        return this.get(ctx);
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("filepath")) {
            o.put("filepath", "");
        }
        if (!o.containsStringParam("fixedPath")) {
            o.put("fixedPath", "false");
        }
        if (!o.containsStringParam("postURL")) {
            o.put("postURL", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    public boolean get(final TestRun ctx) {
        final WebElement el = ctx.locator().find(ctx);
        try {
            this.getDownloadFile(ctx, el.getAttribute("href"));
        } catch (final Throwable e) {
            ctx.log().error("download http get failure cause:", e);
            return false;
        }
        return true;
    }

    public boolean post(final TestRun ctx) {
        try {
            this.postDownloadFile(ctx, ctx.string("postURL"));
        } catch (final Throwable e) {
            ctx.log().error("download http post failure cause:", e);
            return false;
        }
        return true;
    }

    protected void postDownloadFile(final TestRun ctx, final String downloadUrl) throws IOException {
        final HttpRequest req = new HttpRequest(HttpMethod.POST, downloadUrl)
                .addHeader("Content-Type", "application/x-www-from-urlencoded");
        final String contents = ctx.locator()
                .findElements(ctx)
                .stream()
                .filter(element -> element.getAttribute("name") != null)
                .map(element ->
                        URLEncoder.encode(element.getAttribute("name"), StandardCharsets.UTF_8)
                                + "=" +
                                URLEncoder.encode(element.getAttribute("value"), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
        req.setContent(() -> new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8)));
        final HttpResponse res = this.getClient(ctx).execute(req);
        this.downLoadFile(ctx, res);
    }

    protected void getDownloadFile(final TestRun ctx, final String downloadUrl) throws IOException {
        final HttpRequest req = new HttpRequest(HttpMethod.GET, downloadUrl);
        final HttpResponse res = this.getClient(ctx).execute(req);
        this.downLoadFile(ctx, res);
    }

    protected void downLoadFile(final TestRun ctx, final HttpResponse response) throws IOException {
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

    protected HttpClient getClient(final TestRun ctx) {
        final HttpCommandExecutor executor = (HttpCommandExecutor) ctx.driver().getCommandExecutor();
        final Field clientField;
        try {
            clientField = HttpCommandExecutor.class.getDeclaredField("client");
            clientField.setAccessible(true);
            return (HttpClient) clientField.get(executor);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

}
