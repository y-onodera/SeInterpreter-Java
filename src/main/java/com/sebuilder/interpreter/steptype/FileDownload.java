package com.sebuilder.interpreter.steptype;

import com.google.common.base.Charsets;
import com.sebuilder.interpreter.LocatorHolder;
import com.sebuilder.interpreter.TestRun;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Set;

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
        HttpClient httpClient = getHttpClient(ctx);
        HttpPost httpPost = new HttpPost(downloadUrl);
        ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        ctx.locator()
                .findElements(ctx)
                .stream()
                .forEach(element -> {
                    params.add(new BasicNameValuePair(element.getAttribute("name"), element.getAttribute("value")));
                });
        httpPost.setEntity(new UrlEncodedFormEntity(params, Charsets.UTF_8));
        HttpResponse response = httpClient.execute(httpPost);
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
        HttpClient httpClient = getHttpClient(ctx);
        HttpGet httpGet = new HttpGet(downloadUrl);
        HttpResponse response = httpClient.execute(httpGet);
        downLoadFile(ctx, outputFilePath, response);
    }

    public void downLoadFile(TestRun ctx, String outputFilePath, HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            File outputFile = new File(ctx.getListener().getDownloadDirectory(), ctx.getTestRunName() + "_" + outputFilePath);
            try (InputStream inputStream = entity.getContent(); FileOutputStream fileOutputStream = new FileOutputStream(outputFile)) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, read);
                }
            }
        }
    }

    public HttpClient getHttpClient(TestRun ctx) {
        CookieStore cookieStore = seleniumCookiesToCookieStore(ctx.driver());
        return HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();
    }

    /**
     * Get Cookie from WebDriver browser session.
     *
     * @return cookieStore from WebDriver browser session.
     */
    private CookieStore seleniumCookiesToCookieStore(WebDriver driver) {

        Set<Cookie> seleniumCookies = driver.manage().getCookies();
        CookieStore cookieStore = new BasicCookieStore();

        for (Cookie seleniumCookie : seleniumCookies) {
            BasicClientCookie basicClientCookie =
                    new BasicClientCookie(seleniumCookie.getName(), seleniumCookie.getValue());
            basicClientCookie.setDomain(seleniumCookie.getDomain());
            basicClientCookie.setExpiryDate(seleniumCookie.getExpiry());
            basicClientCookie.setPath(seleniumCookie.getPath());
            cookieStore.addCookie(basicClientCookie);
        }

        return cookieStore;
    }

}
