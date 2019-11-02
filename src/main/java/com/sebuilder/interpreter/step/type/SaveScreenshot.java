/*
 * Copyright 2012 Sauce Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sebuilder.interpreter.step.type;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ComparisonResult;
import com.github.romankh3.image.comparison.model.ComparisonState;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.LocatorInnerScrollElementHandler;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.VerticalPrinter;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class SaveScreenshot extends AbstractStepType implements LocatorHolder {

    @Override
    public boolean isContinueAtFailure() {
        return true;
    }

    @Override
    public boolean run(TestRun ctx) {
        RemoteWebDriver wd = ctx.driver();
        Dimension beforeSize = wd.manage().window().getSize();
        this.adjustWindowSize(wd);
        wd.switchTo().defaultContent();
        Page target = new Page(ctx, 100, new LocatorInnerScrollElementHandler(wd));
        try {
            final String fileName = ctx.getTestRunName() + "_" + ctx.string("file");
            File file = ctx.getListener().addScreenshot(fileName);
            BufferedImage actual = target.printImage(new VerticalPrinter(), 0);
            this.reverseWindowSize(wd, beforeSize);
            if (ctx.getBoolean("verify")) {
                BufferedImage expect = ImageComparisonUtil.readImageFromFile(new File(Context.getExpectScreenShotDirectory(), fileName));
                boolean compareResult = this.compare(file, actual, expect);
                if (!compareResult) {
                    File expectFile = ctx.getListener().saveExpectScreenshot();
                    ImageIO.write(expect, "PNG", expectFile);
                }
                return compareResult;
            }
            ImageIO.write(actual, "PNG", file);
            return file.exists();
        } catch (IOException e) {
            ctx.log().error(e);
            this.reverseWindowSize(wd, beforeSize);
            return false;
        }
    }

    @Override
    public StepBuilder addDefaultParam(StepBuilder o) {
        if (!o.containsStringParam("file")) {
            o.put("file", "");
        }
        if (!o.containsStringParam("verify")) {
            o.put("verify", "false");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    protected boolean needMaximize(RemoteWebDriver wd) {
        return wd instanceof InternetExplorerDriver || wd instanceof EdgeDriver;
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

    protected boolean compare(File file, BufferedImage actual, BufferedImage expect) throws IOException {
        if (this.isSizeMissMatch(actual, expect)) {
            BufferedImage resizeActual = this.toSameSize(actual, expect.getWidth(), expect.getHeight());
            ImageIO.write(this.getComparisonResult(file, resizeActual, expect).getResult(), "PNG", file);
            return false;
        }
        ComparisonResult result = this.getComparisonResult(file, actual, expect);
        ImageIO.write(result.getResult(), "PNG", file);
        return result.getComparisonState() == ComparisonState.MATCH;
    }

    protected BufferedImage toSameSize(BufferedImage actual, int expectWidth, int expectHeight) {
        BufferedImage finalImage = new BufferedImage(expectWidth, expectHeight, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();
        graphics.drawImage(actual, 0, 0, null);
        graphics.dispose();
        return finalImage;
    }

    protected ComparisonResult getComparisonResult(File file, BufferedImage actual, BufferedImage expect) throws IOException {
        return new ImageComparison(expect, actual, file)
                .setDrawExcludedRectangles(true)
                .compareImages();
    }

    protected boolean isSizeMissMatch(BufferedImage actual, BufferedImage expect) {
        return actual.getHeight() != expect.getHeight() || actual.getWidth() != expect.getWidth();
    }

    protected void adjustWindowSize(RemoteWebDriver wd) {
        if (this.needMaximize(wd)) {
            wd.manage().window().maximize();
            this.waitForRepaint();
        }
    }
}
