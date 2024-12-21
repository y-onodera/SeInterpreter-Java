package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.http.jdk.JdkHttpClient;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
            String uri = el.getDomAttribute("href");
            URI uri1 = new URI(ctx.driver().getCurrentUrl());
            String baseURL = uri1.toString().replace(uri1.getRawPath(), "");
            if (!uri.startsWith(baseURL)) {
                uri = baseURL + uri;
            }
            this.getDownloadFile(ctx, uri);
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

    protected void postDownloadFile(final TestRun ctx, final String downloadUrl) throws IOException, URISyntaxException, InterruptedException {
        final String contents = ctx.locator()
                .findElements(ctx)
                .stream()
                .filter(element -> element.getDomAttribute("name") != null)
                .map(element -> element.getDomAttribute("name") + "=" + element.getDomAttribute("value"))
                .collect(Collectors.joining("&"));
        this.downLoadFile(ctx, HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .setHeader("Content-Type", "application/x-www-form-urlencoded; charset=utf-8")
                .POST(HttpRequest.BodyPublishers.ofString(contents))
                .build());
    }

    protected void getDownloadFile(final TestRun ctx, final String downloadUrl) throws IOException, URISyntaxException, InterruptedException {
        this.downLoadFile(ctx, HttpRequest.newBuilder()
                .uri(URI.create(downloadUrl))
                .GET().build());
    }

    protected void downLoadFile(final TestRun ctx, final HttpRequest req) throws IOException, URISyntaxException, InterruptedException {
        final HttpClient client = getHttpClient(ctx);
        final HttpResponse<byte[]> res = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofByteArray());
        final File outputFile;
        if (ctx.getBoolean("fixedPath")) {
            outputFile = ctx.getListener().addDownloadFile(ctx.string("filepath"));
        } else {
            outputFile = ctx.getListener().addDownloadFile(ctx.getTestRunName() + "_" + ctx.string("filepath"));
        }
        try (final InputStream inputStream = new ByteArrayInputStream(res.body()); final FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            int read;
            final byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
        }
    }

    private static HttpClient getHttpClient(final TestRun ctx) throws URISyntaxException {
        return getClient(getJdkHttpClient(ctx));
    }

    private static HttpClient getClient(JdkHttpClient client) {
        try {
            Field clientField = JdkHttpClient.class.getDeclaredField("client");
            clientField.setAccessible(true);
            return (HttpClient) clientField.get(client);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static JdkHttpClient getJdkHttpClient(final TestRun ctx) {
        final HttpCommandExecutor executor = getHttpCommandExecutor(ctx);
        try {
            Field clientField = HttpCommandExecutor.class.getDeclaredField("client");
            clientField.setAccessible(true);
            return (JdkHttpClient) clientField.get(executor);
        } catch (final NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError(e);
        }
    }

    private static HttpCommandExecutor getHttpCommandExecutor(TestRun ctx) {
        return (HttpCommandExecutor) ctx.driver().getCommandExecutor();
    }

}
