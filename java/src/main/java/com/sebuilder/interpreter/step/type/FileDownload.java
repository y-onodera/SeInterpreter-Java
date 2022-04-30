package com.sebuilder.interpreter.step.type;

import com.google.common.collect.Lists;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import io.netty.handler.codec.http.cookie.CookieHeaderNames;
import io.netty.handler.codec.http.cookie.DefaultCookie;
import io.netty.util.HashedWheelTimer;
import io.netty.util.Timer;
import io.netty.util.concurrent.DefaultThreadFactory;
import org.asynchttpclient.*;
import org.asynchttpclient.config.AsyncHttpClientConfigDefaults;
import org.asynchttpclient.cookie.CookieStore;
import org.asynchttpclient.cookie.ThreadSafeCookieStore;
import org.asynchttpclient.uri.Uri;
import org.asynchttpclient.util.HttpConstants;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class FileDownload extends AbstractStepType implements ConditionalStep, LocatorHolder {
    private static final Timer TIMER;

    static {
        ThreadFactory threadFactory = new DefaultThreadFactory("netty-client-timer", true);
        TIMER = new HashedWheelTimer(threadFactory, (long) AsyncHttpClientConfigDefaults.defaultHashedWheelTimerTickDuration(), TimeUnit.MILLISECONDS, AsyncHttpClientConfigDefaults.defaultHashedWheelTimerSize());
    }

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
        } catch (Throwable e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    public boolean post(TestRun ctx) {
        try {
            this.postDownloadFile(ctx, ctx.string("post"), ctx.string("filepath"));
        } catch (Throwable e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    public void postDownloadFile(TestRun ctx, String downloadUrl, String outputFilePath) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient client = getHttpClient(ctx, downloadUrl);
        Charset charset = Charset.forName(StandardCharsets.UTF_8.name());
        List<Param> param = Lists.newArrayList();
        ctx.locator()
                .findElements(ctx)
                .stream()
                .forEach(element -> {
                    param.add(new Param(element.getAttribute("name"), element.getAttribute("value")));
                });
        ListenableFuture whenResponse = client.executeRequest(new RequestBuilder(HttpConstants.Methods.POST)
                .setUri(Uri.create(downloadUrl))
                .setCharset(charset)
                .setFormParams(param));
        this.downLoadFile(ctx, outputFilePath, (Response) whenResponse.get());
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("filepath")) {
            o.put("filepath", "");
        }
        if (!o.containsStringParam("fixedPath")) {
            o.put("fixedPath", "false");
        }
        if (!o.containsStringParam("post")) {
            o.put("post", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    public void getDownloadFile(TestRun ctx, String downloadUrl, String outputFilePath) throws IOException, ExecutionException, InterruptedException {
        AsyncHttpClient client = getHttpClient(ctx, downloadUrl);
        ListenableFuture whenResponse = client.executeRequest(new RequestBuilder().setUri(Uri.create(downloadUrl)));
        this.downLoadFile(ctx, outputFilePath, (Response) whenResponse.get());
    }

    public void downLoadFile(TestRun ctx, String outputFilePath, Response response) throws IOException {

        File outputFile;
        if (ctx.getBoolean("fixedPath")) {
            outputFile = ctx.getListener().addDownloadFile(outputFilePath);
        } else {
            outputFile = ctx.getListener().addDownloadFile(ctx.getTestRunName() + "_" + outputFilePath);
        }
        try (InputStream inputStream = response.getResponseBodyAsStream(); FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            int read;
            byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
        }
    }

    public AsyncHttpClient getHttpClient(TestRun ctx, String downloadUrl) {
        Set<org.openqa.selenium.Cookie> seleniumCookies = ctx.driver().manage().getCookies();
        final CookieStore cookies = new ThreadSafeCookieStore();
        final Uri uri = Uri.create(downloadUrl);
        for (org.openqa.selenium.Cookie seleniumCookie : seleniumCookies) {
            DefaultCookie cookie = new DefaultCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            String domain = seleniumCookie.getDomain();
            if (domain.startsWith(".")) {
                domain = domain.substring(1);
            }
            cookie.setDomain(domain);
            if (seleniumCookie.getExpiry() != null) {
                cookie.setMaxAge(seleniumCookie.getExpiry().getTime());
            }
            cookie.setSecure(seleniumCookie.isSecure());
            cookie.setHttpOnly(seleniumCookie.isHttpOnly());
            if (seleniumCookie.getPath() != null) {
                cookie.setPath(seleniumCookie.getPath());
            }
            if (seleniumCookie.getSameSite() != null) {
                cookie.setSameSite(CookieHeaderNames.SameSite.valueOf(seleniumCookie.getSameSite()));
            }
            cookies.add(uri, cookie);
        }
        DefaultAsyncHttpClientConfig.Builder builder = (new DefaultAsyncHttpClientConfig.Builder())
                .setThreadFactory(new DefaultThreadFactory("AsyncHttpClient", true))
                .setUseInsecureTrustManager(true)
                .setAggregateWebSocketFrameFragments(true)
                .setWebSocketMaxBufferSize(2147483647)
                .setWebSocketMaxFrameSize(2147483647)
                .setNettyTimer(TIMER)
                .setCookieStore(cookies)
                .setUseProxyProperties(true)
                .setUseProxySelector(true);
        return Dsl.asyncHttpClient(builder);
    }

}
