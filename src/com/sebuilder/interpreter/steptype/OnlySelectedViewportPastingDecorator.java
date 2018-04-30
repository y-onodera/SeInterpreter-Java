package com.sebuilder.interpreter.steptype;

import com.sebuilder.interpreter.TestRun;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Set;


/**
 */
public class OnlySelectedViewportPastingDecorator extends SelectedViewportPastingDecorator {

    public OnlySelectedViewportPastingDecorator(ShootingStrategy strategy, TestRun ctx) {
        super(strategy, ctx);
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd, Set<Coords> coordsSet) {

        JavascriptExecutor js = (JavascriptExecutor) wd;
        WebElement el = this.targetWebElement();
        Point point = el.getLocation();
        int elementWidth = el.getSize().width;
        int scrollableHeight = this.getScrollableHeight(js);
        int scrollableViewportHeight = getScrollableViewportHeight(js);
        Coords shootingArea = getShootingCoords(coordsSet, elementWidth, scrollableHeight, scrollableViewportHeight);

        BufferedImage finalImage = new BufferedImage(elementWidth, shootingArea.height, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();

        int scrollTimes = (int) Math.ceil(shootingArea.getHeight() / scrollableViewportHeight);
        for (int n = 0; n <= scrollTimes; n++) {
            int nextScrollTop = Math.min(scrollableHeight, scrollableViewportHeight * n);
            scrollVertically(js, nextScrollTop);
            waitForScrolling();
            int currentScrollY = getCurrentScrollY(js);
            int height = this.getViewPortRemainHeight(scrollableHeight, scrollableViewportHeight, nextScrollTop, currentScrollY);
            if (height > 0) {
                BufferedImage part = getShootingStrategy().getScreenshot(wd);
                part = part.getSubimage(point.getX(), point.getY(), elementWidth, height);
                graphics.drawImage(part, 0, currentScrollY, null);
            }
        }

        graphics.dispose();
        return finalImage;
    }

}