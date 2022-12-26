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
        final ThreadFactory threadFactory = new DefaultThreadFactory("netty-client-timer", true);
        TIMER = new HashedWheelTimer(threadFactory, AsyncHttpClientConfigDefaults.defaultHashedWheelTimerTickDuration(), TimeUnit.MILLISECONDS, AsyncHttpClientConfigDefaults.defaultHashedWheelTimerSize());
    }

    @Override
    public boolean doRun(final TestRun ctx) {
        if (ctx.containsKey("postURL")) {
            return this.post(ctx);
        }
        return this.get(ctx);
    }

    public boolean get(final TestRun ctx) {
        final WebElement el = ctx.locator().find(ctx);
        try {
            this.getDownloadFile(ctx, el.getAttribute("href"), ctx.string("filepath"));
        } catch (final Throwable e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    public boolean post(final TestRun ctx) {
        try {
            this.postDownloadFile(ctx, ctx.string("postURL"), ctx.string("filepath"));
        } catch (final Throwable e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    public void postDownloadFile(final TestRun ctx, final String downloadUrl, final String outputFilePath) throws IOException, ExecutionException, InterruptedException {
        final AsyncHttpClient client = this.getHttpClient(ctx, downloadUrl);
        final Charset charset = Charset.forName(StandardCharsets.UTF_8.name());
        final List<Param> param = Lists.newArrayList();
        ctx.locator()
                .findElements(ctx)
                .forEach(element ->
                        param.add(new Param(element.getAttribute("name"), element.getAttribute("value")))
                );
        final ListenableFuture<Response> whenResponse = client.executeRequest(new RequestBuilder(HttpConstants.Methods.POST)
                .setUri(Uri.create(downloadUrl))
                .setCharset(charset)
                .setFormParams(param));
        this.downLoadFile(ctx, outputFilePath, whenResponse.get());
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

    public void getDownloadFile(final TestRun ctx, final String downloadUrl, final String outputFilePath) throws IOException, ExecutionException, InterruptedException {
        final AsyncHttpClient client = this.getHttpClient(ctx, downloadUrl);
        final ListenableFuture<Response> whenResponse = client.executeRequest(new RequestBuilder().setUri(Uri.create(downloadUrl)));
        this.downLoadFile(ctx, outputFilePath, whenResponse.get());
    }

    public void downLoadFile(final TestRun ctx, final String outputFilePath, final Response response) throws IOException {

        final File outputFile;
        if (ctx.getBoolean("fixedPath")) {
            outputFile = ctx.getListener().addDownloadFile(outputFilePath);
        } else {
            outputFile = ctx.getListener().addDownloadFile(ctx.getTestRunName() + "_" + outputFilePath);
        }
        try (final InputStream inputStream = response.getResponseBodyAsStream(); final FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
            int read;
            final byte[] bytes = new byte[1024];
            while ((read = inputStream.read(bytes)) != -1) {
                fileOutputStream.write(bytes, 0, read);
            }
        }
    }

    public AsyncHttpClient getHttpClient(final TestRun ctx, final String downloadUrl) {
        final Set<org.openqa.selenium.Cookie> seleniumCookies = ctx.driver().manage().getCookies();
        final CookieStore cookies = new ThreadSafeCookieStore();
        final Uri uri = Uri.create(downloadUrl);
        for (final org.openqa.selenium.Cookie seleniumCookie : seleniumCookies) {
            final DefaultCookie cookie = new DefaultCookie(seleniumCookie.getName(), seleniumCookie.getValue());
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
        final DefaultAsyncHttpClientConfig.Builder builder = (new DefaultAsyncHttpClientConfig.Builder())
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
