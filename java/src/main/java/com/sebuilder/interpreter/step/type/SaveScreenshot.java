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
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.github.romankh3.image.comparison.model.Rectangle;
import com.google.common.collect.Lists;
import com.sebuilder.interpreter.Context;
import com.sebuilder.interpreter.ImageArea;
import com.sebuilder.interpreter.StepBuilder;
import com.sebuilder.interpreter.TestRun;
import com.sebuilder.interpreter.screenshot.LocatorInnerScrollElementHandler;
import com.sebuilder.interpreter.screenshot.Page;
import com.sebuilder.interpreter.screenshot.VerticalPrinter;
import com.sebuilder.interpreter.step.AbstractStepType;
import com.sebuilder.interpreter.step.LocatorHolder;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class SaveScreenshot extends AbstractStepType implements LocatorHolder {

    @Override
    public boolean isContinueAtFailure() {
        return true;
    }

    @Override
    public boolean run(TestRun ctx) {
        RemoteWebDriver wd = ctx.driver();
        wd.switchTo().defaultContent();
        try {
            final String fileName = ctx.getTestRunName() + "_" + ctx.string("file");
            File file = ctx.getListener().addScreenshot(fileName);
            BufferedImage actual;
            if (ctx.containsKey("scroll") && !ctx.getBoolean("scroll")) {
                if (ctx.hasLocator()) {
                    actual = ctx.getScreenshot(ctx.locator());
                } else {
                    actual = ctx.getScreenshot();
                }
            } else {
                Page target = new Page(ctx, new LocatorInnerScrollElementHandler());
                actual = target.printImage(new VerticalPrinter(), 0);
            }
            if (ctx.getBoolean("verify")) {
                BufferedImage expect = ImageIO.read(new File(Context.getExpectScreenShotDirectory(), fileName));
                boolean compareResult = this.compare(file, actual, expect, ctx);
                File expectFile = ctx.getListener().saveExpectScreenshot(file);
                ImageIO.write(expect, "PNG", expectFile);
                return compareResult;
            }
            ImageIO.write(actual, "PNG", file);
            return file.exists();
        } catch (IOException e) {
            ctx.log().error(e);
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
        if (!o.containsStringParam("scroll")) {
            o.put("scroll", "true");
        }
        if (!o.containsLocatorParam("locatorHeader")) {
            LocatorHolder.super.addDefaultParam("locatorHeader", o);
        }
        if (!o.containsLocatorParam("locatorExclude")) {
            LocatorHolder.super.addDefaultParam("locatorExclude", o);
        }
        if (!o.containsStringParam("imageAreaExclude")) {
            o.put("imageAreaExclude", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    protected boolean compare(File file, BufferedImage actual, BufferedImage expect, TestRun ctx) throws IOException {
        BufferedImage actualResize = actual;
        if (this.isSizeMissMatch(actual, expect)) {
            actualResize = this.toSameSize(actual, expect);
        }
        ImageComparisonResult result = this.getComparisonResult(file, actualResize, expect, ctx);
        if (result.getRectangles() != null) {
            StringBuilder sb = new StringBuilder();
            result.getRectangles()
                    .forEach(it -> sb.append(String.format("[%s,%s,%s,%s]"
                            , it.getMinPoint().getX(), it.getMinPoint().getY()
                            , it.getMaxPoint().getX(), it.getMaxPoint().getY())));
            ctx.getListener().info("diff rectangle:" + sb);
        }
        ImageIO.write(result.getResult(), "PNG", file);
        return result.getImageComparisonState() == ImageComparisonState.MATCH;
    }

    protected BufferedImage toSameSize(BufferedImage actual, BufferedImage expect) {
        BufferedImage finalImage = new BufferedImage(expect.getWidth(), expect.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = finalImage.createGraphics();
        graphics.drawImage(actual, 0, 0, null);
        graphics.dispose();
        return finalImage;
    }

    protected ImageComparisonResult getComparisonResult(File file, BufferedImage actual, BufferedImage expect, TestRun ctx) {
        final List<Rectangle> exclude = Lists.newArrayList();
        double pixelToleranceLevel = 0.1;
        double allowingPercentOfDifferentPixels = 0.0;
        if (ctx.containsKey("imageAreaExclude")) {
            exclude.addAll(new ImageArea(ctx.string("imageAreaExclude")).getRectangles());
        }
        if (ctx.hasLocator("locatorExclude")) {
            ctx.locator("locatorExclude")
                    .findElements(ctx)
                    .forEach(it -> exclude.add(new Rectangle(it.getLocation().getX(), it.getLocation().getY()
                            , it.getLocation().getX() + it.getSize().getWidth(), it.getLocation().getY() + it.getSize().getHeight())));
        }
        if (ctx.containsKey("pixelToleranceLevel")) {
            pixelToleranceLevel = Double.parseDouble(ctx.string("pixelToleranceLevel"));
        }
        if (ctx.containsKey("allowingPercentOfDifferentPixels")) {
            allowingPercentOfDifferentPixels = Double.parseDouble(ctx.string("allowingPercentOfDifferentPixels"));
        }
        return new ImageComparison(expect, actual, file)
                .setExcludedAreas(exclude)
                .setPixelToleranceLevel(pixelToleranceLevel)
                .setAllowingPercentOfDifferentPixels(allowingPercentOfDifferentPixels)
                .setDrawExcludedRectangles(true)
                .compareImages();
    }

    protected boolean isSizeMissMatch(BufferedImage actual, BufferedImage expect) {
        return actual.getHeight() != expect.getHeight() || actual.getWidth() != expect.getWidth();
    }

}
