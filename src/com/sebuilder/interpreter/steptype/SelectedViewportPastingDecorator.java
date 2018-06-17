package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.shooting.ViewportPastingDecorator;
import ru.yandex.qatools.ashot.util.InnerScript;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;

public class SelectedViewportPastingDecorator extends ViewportPastingDecorator {
    protected final TestRun ctx;

    public SelectedViewportPastingDecorator(ShootingStrategy strategy, TestRun ctx) {
        super(strategy);
        this.ctx = ctx;
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd, Set<Coords> coordsSet) {
        JavascriptExecutor js = (JavascriptExecutor) wd;
        int pageWidth = this.getFullWidth(wd);
        int viewportHeight = this.getWindowHeight(wd);
        int scrollableHeight = this.getScrollableHeight(js);
        int scrollableViewportHeight = this.getScrollableViewportHeight(js);
        Coords shootingArea = this.getShootingCoords(coordsSet, pageWidth, viewportHeight + scrollableHeight - scrollableViewportHeight, viewportHeight);

        BufferedImage finalImage = new BufferedImage(pageWidth, shootingArea.height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();

        WebElement el = ctx.locator().find(ctx);
        Point point = el.getLocation();
        int pointY = point.getY();

        if (pointY > 0) {
            BufferedImage overScrollableArea = getShootingStrategy().getScreenshot(wd);
            overScrollableArea = overScrollableArea.getSubimage(0, 0, pageWidth, pointY);
            graphics.drawImage(overScrollableArea, 0, 0, null);
        }
        int scrollTimes = (int) Math.ceil(scrollableHeight / scrollableViewportHeight);
        for (int n = 0; n <= scrollTimes; n++) {
            int nextScrollTop = Math.min(scrollableHeight, scrollableViewportHeight * n);
            scrollVertically(js, nextScrollTop);
            waitForScrolling();
            int currentScrollY = getCurrentScrollY(js);
            int height = this.getViewPortRemainHeight(scrollableHeight, scrollableViewportHeight, nextScrollTop, currentScrollY);
            if (height > 0) {
                BufferedImage part = getShootingStrategy().getScreenshot(wd);
                part = part.getSubimage(0, pointY, pageWidth, height);
                graphics.drawImage(part, 0, pointY + currentScrollY, null);
            }
        }

        int remainHeight = viewportHeight - scrollableViewportHeight - pointY;
        if (remainHeight > 0) {
            BufferedImage underScrollableArea = getShootingStrategy().getScreenshot(wd);
            underScrollableArea = underScrollableArea.getSubimage(0, pointY + scrollableViewportHeight, pageWidth, remainHeight);
            graphics.drawImage(underScrollableArea, 0, pointY + scrollableHeight, null);
        }
        graphics.dispose();
        return finalImage;
    }

    @Override
    public int getFullWidth(WebDriver driver) {
        return  ((Number) JavascriptExecutor.class.cast(driver).executeScript("return document.documentElement.clientWidth || document.getElementsByTagName('body')[0].clientWidth;",  new Object[0])).intValue();
    }

    @Override
    public int getWindowHeight(WebDriver driver) {
        return  ((Number) JavascriptExecutor.class.cast(driver).executeScript("return document.documentElement.clientHeight || document.getElementsByTagName('body')[0].clientHeight;",  new Object[0])).intValue();
    }

    protected WebElement targetWebElement() {
        return ctx.locator().find(ctx);
    }


    protected int getScrollableHeight(JavascriptExecutor driver) {
        return ((Number) driver.executeScript("return Math.max(arguments[0].scrollHeight, arguments[0].offsetHeight);", ctx.locator().find(ctx))).intValue();
    }

    protected int getScrollableViewportHeight(JavascriptExecutor driver) {
        return ((Number) driver.executeScript("return arguments[0].innerHeight || arguments[0].clientHeight;", ctx.locator().find(ctx))).intValue();
    }

    @Override
    protected int getCurrentScrollY(JavascriptExecutor js) {
        return ((Number) js.executeScript("var scrY = arguments[0].scrollTop;"
                + "if(scrY){return scrY;} else {return 0;}", ctx.locator().find(ctx))).intValue();
    }

    @Override
    protected void scrollVertically(JavascriptExecutor js, int scrollY) {
        js.executeScript("arguments[0].scrollTop = arguments[1]; return [];", ctx.locator().find(ctx), scrollY);
    }

    protected int getViewPortRemainHeight(int scrollableHeight, int scrollableViewportHeight, int nextScrollTop, int currentScrollY) {
        if (nextScrollTop != currentScrollY) {
            return scrollableHeight - currentScrollY;
        }
        return scrollableViewportHeight;
    }


    protected Coords getShootingCoords(Set<Coords> coords, int pageWidth, int pageHeight, int viewPortHeight) {
        return coords != null && !coords.isEmpty() ? this.extendShootingArea(Coords.unity(coords), viewPortHeight, pageHeight) : new Coords(0, 0, pageWidth, pageHeight);
    }

    protected Coords extendShootingArea(Coords shootingCoords, int viewportHeight, int pageHeight) {
        int halfViewport = viewportHeight / 2;
        shootingCoords.y = Math.max(shootingCoords.y - halfViewport / 2, 0);
        shootingCoords.height = Math.min(shootingCoords.height + halfViewport, pageHeight);
        return shootingCoords;
    }

    protected void waitForScrolling() {
        try {
            Thread.sleep((long) this.scrollTimeout);
        } catch (InterruptedException var2) {
            throw new IllegalStateException("Exception while waiting for scrolling", var2);
        }
    }
}
