package com.sebuilder.interpreter.step.getter;

import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ComparisonResult;
import com.github.romankh3.image.comparison.model.ComparisonState;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.LocatorInnerScrollElementHandler;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.VerticalPrinter;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ScreenShotCompare extends AbstractGetter {

    @Override
    public String get(TestRun ctx) {
        RemoteWebDriver wd = ctx.driver();
        Dimension beforeSize = wd.manage().window().getSize();
        this.adjustWindowSize(wd);
        wd.switchTo().defaultContent();
        Page target = new Page(ctx, 100, new LocatorInnerScrollElementHandler(wd));
        try {
            File file = ctx.getListener().addScreenshot(ctx.getTestRunName() + "_" + "result.png");
            BufferedImage actual = target.printImage(new VerticalPrinter(), 0);
            this.reverseWindowSize(wd, beforeSize);
            BufferedImage expect = ImageComparisonUtil.readImageFromFile(new File(ctx.string("expect")));
            ComparisonResult result = new com.github.romankh3.image.comparison.ImageComparison(actual, expect, file)
                    .setDrawExcludedRectangles(true)
                    .compareImages();
            return Boolean.toString(result.getComparisonState() == ComparisonState.MATCH);
        } catch (IOException e) {
            ctx.log().error(e);
            this.reverseWindowSize(wd, beforeSize);
            return null;
        }
    }

    protected boolean needMaximize(RemoteWebDriver wd) {
        return wd instanceof InternetExplorerDriver || wd instanceof EdgeDriver;
    }

    protected void adjustWindowSize(RemoteWebDriver wd) {
        if (this.needMaximize(wd)) {
            wd.manage().window().maximize();
            this.waitForRepaint();
        }
    }

    protected void reverseWindowSize(RemoteWebDriver wd, Dimension beforeSize) {
        if (this.needMaximize(wd)) {
            wd.manage().window().setSize(beforeSize);
            this.waitForRepaint();
        }
    }

    protected void waitForRepaint() {
        try {
            Thread.sleep(600);
        } catch (InterruptedException var2) {
            throw new IllegalStateException("Exception while waiting for repaint", var2);
        }
    }
}
