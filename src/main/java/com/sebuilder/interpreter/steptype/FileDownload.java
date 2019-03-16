package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.LocatorHolder;
import okhttp3.*;
import org.json.JSONException;
import org.json.JSONObject;
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

public class FileDownload implements ConditionalStep, LocatorHolder {

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
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
            getDownloadFile(ctx, el.getAttribute("href"), ctx.string("filepath"));
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    public boolean post(TestRun ctx) {
        try {
            postDownloadFile(ctx, ctx.string("post"), ctx.string("filepath"));
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
        downLoadFile(ctx, outputFilePath, response);
    }

    @Override
    public void supplementSerialized(JSONObject o) throws JSONException {
        LocatorHolder.super.supplementSerialized(o);
        if (!o.has("name")) {
            o.put("name", "");
        }
        if (!o.has("value")) {
            o.put("value", "");
        }
        if (!o.has("filepath")) {
            o.put("filepath", "");
        }
        if (!o.has("post")) {
            o.put("post", "");
        }
    }


    public void getDownloadFile(TestRun ctx, String downloadUrl, String outputFilePath) throws IOException {
        OkHttpClient client = getHttpClient(ctx, downloadUrl);
        Request request = new Request.Builder().url(downloadUrl).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        downLoadFile(ctx, outputFilePath, response);
    }

    public void downLoadFile(TestRun ctx, String outputFilePath, Response response) throws IOException {
        ResponseBody body = response.body();
        if (body != null) {
            File outputFile = new File(ctx.getListener().getDownloadDirectory(), ctx.getTestRunName() + "_" + outputFilePath);
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
