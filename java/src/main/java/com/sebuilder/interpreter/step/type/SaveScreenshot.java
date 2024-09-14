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
    public boolean run(final TestRun ctx) {
        final RemoteWebDriver wd = ctx.driver();
        wd.switchTo().defaultContent();
        try {
            final String fileName = this.getResultFileName(ctx);
            final File file = ctx.getListener().addScreenshot(fileName);
            BufferedImage actual = this.takeScreenShot(ctx);
            if (!ctx.containsKey("strict") || !ctx.getBoolean("strict")) {
                BufferedImage check = this.takeScreenShot(ctx);
                while (this.getComparisonResult(actual, check, ctx).getImageComparisonState() != ImageComparisonState.MATCH) {
                    actual = check;
                    check = this.takeScreenShot(ctx);
                }
            }
            if (ctx.getBoolean("verify")) {
                final File expectFile = ctx.containsKey("expect") && !ctx.string("expect").isBlank()
                        ? new File(ctx.string("expect")) : new File(Context.getExpectScreenShotDirectory(), fileName);
                final BufferedImage expect = ImageIO.read(expectFile);
                final boolean compareResult = this.compare(file, actual, expect, ctx);
                final File targetPath = ctx.getListener().saveExpectScreenshot(file);
                ImageIO.write(expect, "PNG", targetPath);
                return compareResult;
            }
            ImageIO.write(actual, "PNG", file);
            return file.exists();
        } catch (final IOException e) {
            ctx.log().error("save image file failed cause:", e);
            return false;
        }
    }

    @Override
    public StepBuilder addDefaultParam(final StepBuilder o) {
        if (!o.containsStringParam("file")) {
            o.put("file", "");
        }
        if (!o.containsStringParam("addPrefix")) {
            o.put("addPrefix", "true");
        }
        if (!o.containsStringParam("scroll")) {
            o.put("scroll", "true");
        }
        if (!o.containsLocatorParam("locatorHeader")) {
            LocatorHolder.super.addDefaultParam("locatorHeader", o);
        }
        if (!o.containsStringParam("strict")) {
            o.put("strict", "true");
        }
        if (!o.containsStringParam("verify")) {
            o.put("verify", "false");
        }
        if (!o.containsStringParam("expect")) {
            o.put("expect", "");
        }
        if (!o.containsLocatorParam("locatorExclude")) {
            LocatorHolder.super.addDefaultParam("locatorExclude", o);
        }
        if (!o.containsStringParam("imageAreaExclude")) {
            o.put("imageAreaExclude", "");
        }
        return o.apply(LocatorHolder.super::addDefaultParam);
    }

    protected String getResultFileName(final TestRun ctx) {
        String fileName = ctx.string("file");
        if (!ctx.containsKey("addPrefix") || ctx.getBoolean("addPrefix")) {
            fileName = ctx.getTestRunName() + "_" + fileName;
        }
        return fileName;
    }

    protected BufferedImage takeScreenShot(final TestRun ctx) {
        final BufferedImage actual;
        if (ctx.containsKey("scroll") && !ctx.getBoolean("scroll")) {
            if (ctx.hasLocator()) {
                actual = ctx.getScreenshot(ctx.locator());
            } else {
                actual = ctx.getScreenshot();
            }
        } else {
            final Page target = new Page(ctx, new LocatorInnerScrollElementHandler());
            actual = target.printImage(new VerticalPrinter(), 0);
        }
        return actual;
    }

    protected boolean compare(final File file, final BufferedImage actual, final BufferedImage expect, final TestRun ctx) throws IOException {
        final ImageComparisonResult result = this.getComparisonResult(actual, expect, ctx);
        if (result.getRectangles() != null) {
            final StringBuilder sb = new StringBuilder();
            result.getRectangles()
                    .forEach(it -> sb.append(String.format("[%s,%s,%s,%s]"
                            , it.getMinPoint().getX(), it.getMinPoint().getY()
                            , it.getMaxPoint().getX(), it.getMaxPoint().getY())));
            ctx.getListener().info("diff rectangle:" + sb);
        }
        ImageIO.write(result.getResult(), "PNG", file);
        return result.getImageComparisonState() == ImageComparisonState.MATCH;
    }

    protected ImageComparisonResult getComparisonResult(final BufferedImage actual, final BufferedImage expect, final TestRun ctx) {
        BufferedImage expectResize = expect;
        if (this.isSizeMissMatch(actual, expect)) {
            expectResize = ImageComparisonUtil.resize(expect, actual.getWidth(), actual.getHeight());
        }
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
        return new ImageComparison(expectResize, actual)
                .setExcludedAreas(exclude)
                .setPixelToleranceLevel(pixelToleranceLevel)
                .setAllowingPercentOfDifferentPixels(allowingPercentOfDifferentPixels)
                .setDrawExcludedRectangles(true)
                .compareImages();
    }

    protected boolean isSizeMissMatch(final BufferedImage actual, final BufferedImage expect) {
        return actual.getHeight() != expect.getHeight() || actual.getWidth() != expect.getWidth();
    }

}
