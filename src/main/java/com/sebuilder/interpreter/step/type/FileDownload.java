package com.sebuilder.interpreter.step.type;

import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import okhttp3.*;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.net.CookiePolicy.ACCEPT_ALL;

public class FileDownload extends AbstractStepType implements ConditionalStep, LocatorHolder {

    @Override
    public boolean doRun(TestRun ctx) {
        if (ctx.containsKey("post")) {
            return post(ctx);
        }
        return get(ctx);
    }

    public boolean get(TestRun ctx) {
        WebElement el = ctx.locator().find(ctx);
        try {
            this.getDownloadFile(ctx, el.getAttribute("href"), ctx.string("filepath"));
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    public boolean post(TestRun ctx) {
        try {
            this.postDownloadFile(ctx, ctx.string("post"), ctx.string("filepath"));
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    public void postDownloadFile(TestRun ctx, String downloadUrl, String outputFilePath) throws IOException {
        OkHttpClient client = getHttpClient(ctx, downloadUrl);
        Charset charset = Charset.forName(StandardCharsets.UTF_8.name());
        FormBody.Builder requestBody = new FormBody.Builder(charset);
        ctx.locator()
                .findElements(ctx)
                .stream()
                .forEach(element -> {
                    requestBody.add(element.getAttribute("name"), element.getAttribute("value"));
                });
        Request request = new Request.Builder().url(downloadUrl).post(requestBody.build()).build();
        Response response = client.newCall(request).execute();
        this.downLoadFile(ctx, outputFilePath, response);
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("name")) {
            o.put("name", "");
        }
        if (!o.containsStringParam("value")) {
            o.put("value", "");
        }
        if (!o.containsStringParam("filepath")) {
            o.put("filepath", "");
        }
        if (!o.containsStringParam("post")) {
            o.put("post", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    public void getDownloadFile(TestRun ctx, String downloadUrl, String outputFilePath) throws IOException {
        OkHttpClient client = getHttpClient(ctx, downloadUrl);
        Request request = new Request.Builder().url(downloadUrl).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        this.downLoadFile(ctx, outputFilePath, response);
    }

    public void downLoadFile(TestRun ctx, String outputFilePath, Response response) throws IOException {
        ResponseBody body = response.body();
        if (body != null) {
            File outputFile;
            if (ctx.getBoolean("fixedPath")) {
                outputFile = ctx.getListener().addDownloadFile(outputFilePath);
            } else {
                outputFile = ctx.getListener().addDownloadFile(ctx.getTestRunName() + "_" + outputFilePath);
            }
            try (InputStream inputStream = body.byteStream(); FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, read);
                }
            }
        }
    }

    public OkHttpClient getHttpClient(TestRun ctx, String downloadUrl) {
        List<Cookie> cookies = new ArrayList();
        Set<org.openqa.selenium.Cookie> seleniumCookies = ctx.driver().manage().getCookies();
        for (org.openqa.selenium.Cookie seleniumCookie : seleniumCookies) {
            Cookie.Builder builder = new Cookie.Builder()
                    .name(seleniumCookie.getName())
                    .value(seleniumCookie.getValue());
            String domain = seleniumCookie.getDomain();
            if (domain.startsWith(".")) {
                domain = domain.substring(1);
            }
            builder.domain(domain);
            if (seleniumCookie.getExpiry() != null) {
                builder.expiresAt(seleniumCookie.getExpiry().getTime());
            }
            if (seleniumCookie.isSecure()) {
                builder.secure();
            }
            if (seleniumCookie.isHttpOnly()) {
                builder.httpOnly();
            }
            builder.path(seleniumCookie.getPath());
            cookies.add(builder.build());
        }
        CookieManager cookieManager = new CookieManager(null, ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        CookieJar cookieJar = new JavaNetCookieJar(cookieManager);
        cookieJar.saveFromResponse(HttpUrl.parse(downloadUrl), cookies);
        return new OkHttpClient().newBuilder().cookieJar(cookieJar).build();
    }

}
