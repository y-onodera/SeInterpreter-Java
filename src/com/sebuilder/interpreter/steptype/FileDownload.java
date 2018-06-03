package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.TestRun;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

public class FileDownload implements ConditionalStep {

    /**
     * Perform the action this step consists of.
     *
     * @param ctx Current test run.
     * @return Whether the step succeeded. This should be true except for failed verify steps, which
     * should return false. Other failures should throw a RuntimeException.
     */
    @Override
    public boolean doRun(TestRun ctx) {
        WebElement el = ctx.locator().find(ctx);
        try {
            downloadFile(ctx,el.getAttribute("href"),ctx.string("filepath"));
        } catch (IOException e) {
            ctx.log().error(e);
            return false;
        }
        return true;
    }

    public  void downloadFile(TestRun ctx,String downloadUrl, String outputFilePath) throws IOException {
        CookieStore cookieStore = seleniumCookiesToCookieStore(ctx.driver());
        HttpClient httpClient = HttpClientBuilder.create()
                .setDefaultCookieStore(cookieStore)
                .build();
        HttpGet httpGet = new HttpGet(downloadUrl);
        HttpResponse response = httpClient.execute(httpGet);
        HttpEntity entity = response.getEntity();
        if (entity != null) {
            File outputFile = new File(outputFilePath);
            try(InputStream inputStream = entity.getContent();FileOutputStream fileOutputStream = new FileOutputStream(outputFile);) {
                int read;
                byte[] bytes = new byte[1024];
                while ((read = inputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, read);
                }
            }
        }
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
